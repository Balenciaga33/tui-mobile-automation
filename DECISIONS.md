# Decisions

## Why this project structure

One Maven test module. Reviewers run `mvn clean test`; there is no separate “framework JAR”.

Layers match how a mobile team would keep this after the assignment:

- `features/` — five release checks in business language
- `steps/` — glue only; no locators
- `pages/` — one class per screen/dialog (POM)
- `driver/` — session, local Appium server, device; Android / iOS / cloud stay out of steps
- `config/config.yaml` — environment
- `testdata/*.json` — users and expected offers (single source of truth for anchors)
- `runner/` — entry point so Maven discovers and runs the Cucumber suite

Tags: `@login`, `@offers`, `@smoke` (happy login + All tab).  
Short gate: `mvn test -Dcucumber.filter.tags="@smoke"`.

Appium is a **project** `npm` dependency, not a global daemon. Hooks start it from `node_modules`, so clone → `npm install` → `mvn clean test` is enough.

iOS is wired in `DriverManager` (XCUITest caps, bundle id, `.ipa` path) and fails with a clear message because only an Android APK was provided. Structure is ready; I did not invent an iOS app.

## Which parts used AI tools, and where I corrected the output

AI (Cursor) was used to:

- explore the APK (screens, test tags, bundled JSON)
- scaffold the Maven / Cucumber / POM layout
- draft README and this file

I steered or overrode the output where it would have been wrong for *this* app:

- **DOB automation.** Drafts tried typing into the login DOB field (it is `readOnly`) or sending `10/21/1990` with slashes. Working flow: open the calendar, enter digits only (`10211990`), dismiss the keyboard, then Confirm — otherwise the tap hits the IME.
- **One strong negative login case.** Missing DOB (with username/password filled) proves the calendar gate — not three empty-field clones of the same `Required` check.
- **Stable locators + tab state.** Compose `testTag` ids; no fragile XPath. Also assert the tab’s `selected` attribute — cards visible after a click do not prove the right filter is active.
- **Offers in JSON, not Gherkin.** Hotel/holiday/board anchors come from `offers.json`; filter scenarios also assert the other product type is absent.
- **Clone-and-run wiring.** Appium starts from `node_modules` (not a second terminal). Maven entry point fixed after `Tests run: 0`.
- **`@smoke` as a short gate.** Happy login + All tab for a fast check; do not treat all five scenarios as the default micro-PR run. Full set stays `@login` / `@offers`.
- **Idempotent UiAutomator2 install in CI.** Blind `appium driver install` failed when `npm ci` had already provided the driver — install only if missing.
- **Only what the APK can do.** No search box, real booking, or iOS without an `.ipa`. Price/book UI is a no-op here.

## What I would add given more time

- Scroll + assert beyond the first viewport of bundled results
- Light accessibility checks for Compose nodes that have no test tag
- Contract checks: APK `holiday-results.json` / `hotel-results.json` vs `offers.json`
- iOS GitHub Actions job once an `.ipa` exists (macOS runner + simctl), same Gherkin
- Cloud matrix (`-Dexecution=cloud`) with BrowserStack/Sauce secrets

## How I would run this in CI with parallel iOS and Android

**Android is wired:** [`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml) runs JDK 21 + npm + an API 34 emulator, a short UiAutomator preflight that the login screen is visible, then `mvn clean test`. Failures upload screenshots and page source.

Same command across platforms; only capabilities and runner OS change:

```text
mvn clean test -Dplatform=${{ matrix.platform }} -Dexecution=local
```

Intended matrix (iOS blocked on missing `.ipa`):

1. **Matrix:** `{ platform: android }` (done) and `{ platform: ios }` (future).
2. **Android job:** as in `android-ci.yml`.
3. **iOS job:** macOS runner, simctl, install `.ipa`/`.app`, `mvn clean test -Dplatform=ios`.
4. **Cloud:** same matrix with `-Dexecution=cloud` and vendor secrets — parallelism is then N Android × M iOS devices on the farm.
5. **Isolation:** one Appium driver session per job. Parallelise at **pipeline job** level first — do not share one session across Android and iOS.

## Why these five scenarios, not others

The app has two real surfaces: a login form and a tabbed results list fed by local JSON. No search box, no payment, no account API.

| Scenario | Release risk it covers |
| --- | --- |
| Username + password, no DOB → only DOB `Required` | Calendar gate is real; filled fields are not falsely errored |
| Valid form shows selected date (`1990-10-21`), then results | DOB text-input + Confirm binding; happy path into the product |
| All tab selected + hotel + holiday from `offers.json` | Default tab state and mixed feed |
| Hotels: hotel + board + no holiday anchor | Filter inclusion and exclusion |
| Holidays: holiday + price + no hotel anchor | Second product line, CTA, and filter exclusion |

I dropped:

- Empty-form → all `Required` (weak signal: same non-empty check three times)
- Extra login permutations beyond the DOB-missing case
- Booking/price tap (button handler is empty)
- Image loading (network/CDN, not a release gate for this APK)

Five checks, five different failure modes: targeted validation, entry + DOB binding, mixed list + tab state, hotel filter, holiday filter + commercial CTA.
