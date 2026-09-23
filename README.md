# TUI mobile test automation

![Android CI](https://github.com/Balenciaga33/tui-mobile-automation/actions/workflows/android-ci.yml/badge.svg)

Appium + Java + Cucumber BDD + Maven for `TUIChallengeApp.apk`.

Five scenarios on the release-critical path: login validation, successful login, and All / Hotels / Holidays results the app actually ships.

## Prerequisites

| Tool | Version used here |
| --- | --- |
| JDK | 21 |
| Maven | 3.9+ |
| Node.js | 18+ (local Appium from `npm`) |
| Android SDK | platform-tools, build-tools, emulator **or** a real device |

`java`, `mvn`, `adb`, and `emulator` must be on your `PATH`.

```bash
# macOS (typical)
export ANDROID_HOME="$HOME/Library/Android/sdk"
# Linux (typical)
# export ANDROID_HOME="$HOME/Android/Sdk"

export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
```

```bash
java -version   # 21
mvn -version
adb version
```

## Clone and run

```bash
git clone https://github.com/Balenciaga33/tui-mobile-automation.git
cd tui-mobile-automation
npm install
```

Start any Android emulator **or** plug in a phone with USB debugging (`adb devices` must show a device):

```bash
# Option A — example AVD name from config.yaml (any booted emulator also works)
emulator -avd tui_pixel_34
adb wait-for-device

# Option B — helper script
chmod +x scripts/start-emulator.sh
./scripts/start-emulator.sh
```

Appium starts from `node_modules` inside the suite — no global Appium install.

```bash
mvn clean test
```

By tag:

```bash
mvn clean test -Dcucumber.filter.tags="@smoke"
mvn clean test -Dcucumber.filter.tags="@login"
mvn clean test -Dcucumber.filter.tags="@offers"
```

Report: `target/cucumber-report.html`. Failed scenarios also write a PNG under `target/screenshots/`.

## CI

[`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml) runs the suite on an API 34 emulator and uploads the Cucumber report. iOS parallel execution is described in [DECISIONS.md](DECISIONS.md); no `.ipa` was provided.

## Other targets

```bash
# Real device (USB) — id from: adb devices
ANDROID_UDID=<device-serial> mvn clean test -Dexecution=real

# Cloud (BrowserStack example)
CLOUD_URL="https://USER:KEY@hub.browserstack.com/wd/hub" mvn clean test -Dexecution=cloud

# iOS caps are ready; needs an .ipa (not provided)
mvn clean test -Dplatform=ios
```

Config: `src/test/resources/config/config.yaml`.  
Users / offer anchors: `src/test/resources/testdata/*.json`.

## The five scenarios

1. Username + password without DOB → only date of birth shows `Required`.
2. Valid form shows the selected DOB, then opens search results.
3. All tab shows a hotel and a holiday from `offers.json`.
4. Hotels tab shows hotel-only results (name + board from `offers.json`).
5. Holidays tab shows a holiday from `offers.json`, a price CTA, and no hotel card.

Tags: `@login`, `@offers`, `@smoke` (happy login + All tab).

Login does **not** use a secret account — the app only checks non-empty fields. DOB is set via the calendar text-input (`MM/DD/YYYY`).

## Project layout

```
apps/TUIChallengeApp.apk
src/test/java/com/tui/automation/
  config/     YAML + system-property config
  driver/     Appium server, device guard, Android / iOS / cloud
  pages/      Page objects
  steps/      Cucumber steps (no locators)
  hooks/      Session lifecycle + failure attachments
  data/       Loaders for JSON test data
  runner/     Suite entry point for mvn clean test
src/test/resources/com/tui/automation/features/   # the five scenarios
src/test/resources/testdata/
src/test/resources/config/
```

See [DECISIONS.md](DECISIONS.md) for structure, scenario choice, AI use, and CI parallel iOS/Android.
