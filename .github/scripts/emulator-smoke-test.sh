#!/usr/bin/env bash
set -Eeuo pipefail

APK="app/build/outputs/apk/debug/app-debug.apk"
PACKAGE="com.sanskritisathi.app"

adb start-server
adb wait-for-device

# Wait up to 5 minutes for Android and Package Manager to become ready.
ready=0
for attempt in $(seq 1 60); do
  boot_state="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
  package_check="$(adb shell pm list packages 2>&1 || true)"
  if [[ "$boot_state" == "1" ]] && ! grep -qiE 'Broken pipe|Failure calling service|error:' <<<"$package_check"; then
    ready=1
    echo "Android boot and Package Manager ready."
    break
  fi
  echo "Waiting for Android services ($attempt/60)..."
  sleep 5
done
[[ "$ready" == "1" ]] || { echo "Android/Package Manager did not become ready."; adb devices -l; exit 1; }

installed=0
for attempt in 1 2 3; do
  if adb install -r -t "$APK"; then
    installed=1
    break
  fi
  echo "APK install attempt $attempt failed; reconnecting ADB..."
  adb reconnect || true
  adb wait-for-device
  sleep 10
done
[[ "$installed" == "1" ]] || { echo "APK installation failed after retries."; exit 1; }

adb shell am force-stop "$PACKAGE"
adb shell monkey -p "$PACKAGE" 1
sleep 15
adb shell pidof "$PACKAGE" >/dev/null
adb logcat -d -t 3000 > /tmp/sanskriti-logcat.txt
if grep -E 'FATAL EXCEPTION|Process: com[.]sanskritisathi[.]app|Fatal signal' /tmp/sanskriti-logcat.txt; then
  echo "Fatal app crash detected in logcat."
  exit 1
fi
echo "APK installed and app launch smoke test passed."
