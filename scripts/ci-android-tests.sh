#!/usr/bin/env bash
set -euo pipefail

mvn -B clean test -Dplatform=android -Dexecution=local

if [[ ! -f target/cucumber.json ]]; then
  echo "Missing target/cucumber.json — Cucumber did not produce a report" >&2
  exit 1
fi

python3 - <<'PY'
import json
import sys

features = json.load(open("target/cucumber.json"))
scenarios = sum(len(f.get("elements", [])) for f in features)
print(f"Cucumber scenarios executed: {scenarios}")
sys.exit(0 if scenarios > 0 else 1)
PY
