#!/usr/bin/env bash
set -euo pipefail

echo "==> Environment"
java -version
mvn -version
adb devices
ls -la apps/
ls -la src/test/resources/com/tui/automation/features/
npx appium -v

echo "==> Ensure UiAutomator2 driver is available"
npx appium driver list --installed
if ! npx appium driver list --installed --json 2>/dev/null | grep -q '"uiautomator2"'; then
  npx appium driver install uiautomator2
fi
npx appium driver list --installed

echo "==> Run Maven suite"
mvn -B clean test -Dplatform=android -Dexecution=local | tee target-mvn-test.log
MVN_EXIT=${PIPESTATUS[0]}

echo "==> Maven finished with exit code ${MVN_EXIT}"
if [[ -f target/cucumber.json ]]; then
  python3 - <<'PY'
import json
import sys

features = json.load(open("target/cucumber.json"))
elements = [e for f in features for e in f.get("elements", [])]
scenarios = [e for e in elements if e.get("type") in (None, "scenario", "scenario_outline")]
# Cucumber JVM may omit type; count non-background elements
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

exit "${MVN_EXIT}"
