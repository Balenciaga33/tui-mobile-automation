# TUI mobile test automation

![Android CI](https://github.com/Balenciaga33/tui-mobile-automation/actions/workflows/android-ci.yml/badge.svg)

Appium + Java + Cucumber BDD + Maven framework for `TUIChallengeApp.apk`.

Exactly five scenarios cover the release-critical path: login validation, successful login, and the All / Hotels / Holidays results the app actually ships.

## What you need

| Tool | Version used here |
| --- | --- |
| JDK | 21 |
| Maven | 3.9+ |
| Node.js | 18+ (for the local Appium server) |
| Android SDK | platform-tools, build-tools, and an emulator **or** a real device |

macOS PATH example:

```bash
export JAVA_HOME="$HOME/.local/opt/jdk-21/Contents/Home"   # or your JDK 21
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
```

Check:

```bash
java -version
mvn -version
adb version
```

## Clone and run (about 10–15 minutes if the SDK is already installed)

```bash
git clone https://github.com/Balenciaga33/tui-mobile-automation.git
cd tui-mobile-automation
npm install
```

Start an Android emulator **or** plug in a phone with USB debugging:

```bash
# Option A — emulator created as tui_pixel_34
emulator -avd tui_pixel_34
adb wait-for-device

# Option B — helper script
chmod +x scripts/start-emulator.sh
./scripts/start-emulator.sh
```

Run the suite. Appium is started by the tests from `node_modules` — you do not need a global Appium install.

```bash
mvn clean test
```

Selective runs by tag:

```bash
mvn clean test -Dcucumber.filter.tags="@smoke"
mvn clean test -Dcucumber.filter.tags="@login"
mvn clean test -Dcucumber.filter.tags="@offers"
```

HTML report:

```
target/cucumber-report.html
```

Failed scenarios also store a PNG under `target/screenshots/`.

## CI

GitHub Actions workflow [`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml) runs the suite on an API 34 emulator and uploads the Cucumber report. iOS is described in [DECISIONS.md](DECISIONS.md) but not enabled until an `.ipa` is available.

## How to run on different targets

```bash
# Default: local Android emulator / first connected device
mvn clean test

# Real device (USB). Find the id with: adb devices
ANDROID_UDID=emulator-5554 mvn clean test -Dexecution=real

# Cloud (BrowserStack example)
CLOUD_URL="https://USER:KEY@hub.browserstack.com/wd/hub" mvn clean test -Dexecution=cloud

# iOS is wired in DriverManager but needs an .ipa (not provided)
mvn clean test -Dplatform=ios
```

Config lives in `src/test/resources/config/config.yaml`. Test users and expected offers live in `src/test/resources/testdata/*.json`.

## The five scenarios

1. Username + password without DOB → only date of birth shows `Required`.
2. Valid form shows the selected DOB on the field, then opens search results.
3. All tab shows both a hotel and a holiday from `offers.json`.
4. Hotels tab shows hotel-only results (name + board from `offers.json`).
5. Holidays tab shows a holiday from `offers.json`, a price CTA, and no hotel card.

Tags: `@login`, `@offers`, `@smoke` (happy login + All tab). Filter with `-Dcucumber.filter.tags="@smoke"`.

Login does **not** use a secret account. The app only checks that the three fields are non-empty. Date of birth is set via the calendar text-input (`MM/DD/YYYY`).

## Project layout

```
apps/TUIChallengeApp.apk
src/test/java/com/tui/automation/
  config/          YAML + system-property config
  driver/          Appium server, device guard, Android/iOS/cloud factory
  pages/           Page objects
  steps/           Cucumber steps
  hooks/           Session + report attachments
  data/            JSON test data
  runner/          JUnit Platform + Cucumber suite entry point
src/test/resources/com/tui/automation/features/
src/test/resources/testdata/
src/test/resources/config/
```

See [DECISIONS.md](DECISIONS.md) for why this shape, scenario choice, AI use, and CI parallel iOS/Android.
