#!/usr/bin/env bash
set -euo pipefail

AVD_NAME="${1:-tui_pixel_34}"
ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}"
export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$PATH"

if adb devices | awk 'NR>1 && $2=="device" {found=1} END {exit !found}'; then
  echo "Android device already connected"
  exit 0
fi

echo "Starting emulator $AVD_NAME"
emulator -avd "$AVD_NAME" -netdelay none -netspeed full -no-snapshot-load >/tmp/tui-emulator.log 2>&1 &
adb wait-for-device
until [[ "$(adb shell getprop sys.boot_completed | tr -d '\r')" == "1" ]]; do
  sleep 3
done
echo "Emulator is ready"
