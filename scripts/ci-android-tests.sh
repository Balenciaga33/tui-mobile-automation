#!/usr/bin/env bash
set -euo pipefail

PKG=com.tui.qa.challenge
APK=apps/TUIChallengeApp.apk

group() { echo "::group::$1"; }
endgroup() { echo "::endgroup::"; }

group "1/4 Environment"
java -version
mvn -version
adb devices
ls -la apps/
ls -la src/test/resources/com/tui/automation/features/
npx appium -v
command -v node
node -v
endgroup

group "2/4 Appium UiAutomator2 driver"
npx appium driver list --installed
if ! npx appium driver list --installed --json 2>/dev/null | grep -q '"uiautomator2"'; then
  npx appium driver install uiautomator2
fi
npx appium driver list --installed
endgroup

group "3/4 Emulator ready + APK preflight"
adb wait-for-device
adb shell input keyevent KEYCODE_WAKEUP || true
adb shell wm dismiss-keyguard || true
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

adb install -r -t "$APK"
adb shell am force-stop "$PKG" || true
adb logcat -c || true
adb shell am start -W -n "${PKG}/.MainActivity"
# Compose needs a moment on software GPU before semantics are published.
for i in 1 2 3 4 5 6 7 8 9 10; do
  if adb exec-out uiautomator dump /dev/tty 2>/dev/null | tee "target-preflight-ui.xml" | grep -q "login_form_screen_root"; then
    break
  fi
  echo "  waiting for login_form_screen_root (attempt ${i}/10)..."
  sleep 2
done
if ! grep -q "login_form_screen_root" target-preflight-ui.xml; then
  echo "Preflight failed: login_form_screen_root not in UiAutomator dump" >&2
  adb shell dumpsys package "$PKG" | head -80 || true
  adb shell dumpsys activity activities | head -80 || true
  adb logcat -d -t 200 '*:E' || true
  endgroup
  exit 1
fi
echo "Preflight OK: login_form_screen_root is visible to UiAutomator"
adb shell am force-stop "$PKG" || true
endgroup

group "4/4 Cucumber / Maven tests"
mkdir -p target
mvn -B clean test -Dplatform=android -Dexecution=local | tee target-mvn-test.log
MVN_EXIT=${PIPESTATUS[0]}
echo "Maven finished with exit code ${MVN_EXIT}"
endgroup

if [[ "${MVN_EXIT}" -ne 0 ]]; then
  group "Diagnostics (logcat errors)"
  adb logcat -d -t 300 '*:E' || true
  endgroup
fi

group "Cucumber summary"
if [[ -f target/cucumber.json ]]; then
  python3 - <<'PY'
import json
import sys

features = json.load(open("target/cucumber.json"))
elements = [e for f in features for e in f.get("elements", [])]
scenarios = [e for e in elements if e.get("type") != "background"]
passed = failed = skipped = 0
for sc in scenarios:
    statuses = [x.get("result", {}).get("status") for x in sc.get("steps", []) if x.get("result")]
    if any(s == "failed" for s in statuses):
        failed += 1
    elif any(s == "skipped" for s in statuses) and not any(s == "passed" for s in statuses):
        skipped += 1
    else:
        passed += 1
print(f"Cucumber scenarios: total={len(scenarios)} passed={passed} failed={failed} skipped={skipped}")
if len(scenarios) == 0:
    sys.exit(2)
PY
else
  echo "Missing target/cucumber.json" >&2
  MVN_EXIT=1
fi
endgroup

exit "${MVN_EXIT}"
