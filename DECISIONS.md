# Decisions

## Why this project structure

The suite is a single Maven test module. Reviewers run `mvn clean test`; there is no extra “framework JAR” to publish.

Layers are split the way a mobile team would keep them after the task:

- `com/tui/automation/features/` — the five release checks, in business language (classpath package for JUnit Platform discovery)
- `steps/` — glue only; no locators
- `pages/` — one class per screen/dialog (POM)
- `driver/` — session + server + device, so Android / iOS / cloud do not leak into steps
- `config/config.yaml` — environment
- `testdata/*.json` — users and expected offers (single source of truth for anchors)
- `runner/CucumberTest` — JUnit Platform `@Suite` + `cucumber-junit-platform-engine` (no JUnit 4 / Vintage)
- `cucumber.properties` — glue, plugins, long naming strategy for Surefire

Tags: `@login` (login feature), `@offers` (All / Hotels / Holidays cards), `@smoke` (happy login + All tab).  
`mvn test -Dcucumber.filter.tags="@smoke"` for a short gate.

Appium is a **project** `npm` dependency, not a global daemon. Hooks start `AppiumDriverLocalService` from `node_modules` so `mvn clean test` is enough after `npm install`.

Surefire uses `includeJUnit5Engines=junit-platform-suite` so features are not discovered twice (Suite + raw Cucumber engine). Maven reports `Tests run: 5`.

iOS is a first-class branch in `DriverManager` (XCUITest options, bundle id, `.ipa` path). It fails with a clear message because TUI only sent an Android APK. That is intentional: the structure is ready; I did not invent an iOS app.

## Which parts used AI tools, and where I corrected the output

AI (Cursor) was used to:

- inspect the APK (Compose screens, test tags, JSON assets, login ViewModel)
- scaffold Maven / Cucumber / page objects
- draft README and this file

I corrected or overrode the AI output on:

- **Login “credentials”.** First instinct was to hunt for a hardcoded user. The ViewModel only checks non-empty fields — any username/password work. There is no secret account.
- **Date of birth.** The login field is `readOnly`; the date must come from the Material calendar. Typing into the closed field does nothing useful. In the dialog, **text-input mode** accepts `MM/DD/YYYY` as digits only (`10211990` → mask `10/21/1990`); slashes break IME input. Confirm must be tapped **after** the keyboard is dismissed, or the tap hits the IME.
- **Locators.** Prefer `testTag` resource ids (`username_input_field`, `top_app_bar_hotels_tab`, `content_card_hotel_name_{index}`). Card search walks indexed ids, not XPath `contains(@resource-id,…)`. Tab selection reads the `selected` attribute on `top_app_bar_{tab}_tab`.
- **Test data.** Hotel / holiday / board expectations live in `offers.json`; Gherkin says “first hotel / first holiday / expected board”, steps read `TestData`. Filter asserts use the opposite anchor (`no holiday` / `no hotel`).
- **Appium process.** A generated README that said “start Appium in another terminal” would break the 10–15 minute clone-and-run bar. The suite starts the server itself.
- **Runner stack.** An early scaffold mixed Cucumber-JUnit4 + Vintage + Jupiter asserts (Surefire `Tests run: 0`). Migrated to JUnit Platform Suite + Cucumber engine so Maven counts match the five scenarios.
- **Scenario count.** AI drafts tend to add search, filters, and booking. Price/book is a no-op in this APK. I kept five checks that actually exist.

## What I would add given more time (describe only)

- Scroll + assert more than the first viewport of bundled results
- Accessibility snapshot for Compose nodes that have no test tag
- Allure or Masterthought trend reports next to the Cucumber HTML
- Contract tests on APK `holiday-results.json` / `hotel-results.json` vs `offers.json`
- iOS GitHub Actions job once an `.ipa` exists (macOS runner + simctl), reusing the same Gherkin
- Cloud matrix (`-Dexecution=cloud`) with BrowserStack/Sauce secrets

## How I would run this in CI with parallel iOS and Android

**Android is wired:** [`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml) runs `npm ci` + JDK 21 + API 34 emulator (`reactivecircus/android-emulator-runner`) + `mvn clean test`, then uploads the Cucumber HTML/JSON and failure screenshots.

Keep one command across platforms; diverge only on capabilities / runner OS:

```text
mvn clean test -Dplatform=${{ matrix.platform }} -Dexecution=local
```

Intended full matrix (iOS still blocked on missing `.ipa`):

1. **Matrix:** `{ platform: android }` (done) and `{ platform: ios }` (future).
2. **Android job:** as in `android-ci.yml` above.
3. **iOS job:** macOS runner, simctl boot, install `.ipa`/`.app`, `mvn clean test -Dplatform=ios`.
4. **Cloud option:** same matrix but `-Dexecution=cloud` and vendor secrets. Parallelism is then N Android × M iOS devices on the farm.
5. **Isolation:** `DriverManager` is `ThreadLocal`. Do **not** share one Appium session across platforms; parallelise at the **pipeline job** level first.

I would not mix Android and iOS in one JVM process: different Appium drivers and different device farms.

## Why these five scenarios, not others

The app has two real surfaces: a login form and a tabbed results list fed by local JSON. There is no search box, no payment, no account API.

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
- Pagination (same card widget, more rows)

Five checks, each a different failure mode: targeted validation, entry + DOB binding, mixed list + tab state, hotel filter, holiday filter + commercial CTA.
