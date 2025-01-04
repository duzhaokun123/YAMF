[![Chat](https://img.shields.io/badge/Chat-Telegram-blue.svg?logo=telegram)](https://t.me/+HjGegWE9jBM0N2Rl)
[![Size](https://img.shields.io/github/languages/code-size/JuanArton/reYAMF)](https://github.com/JuanArton/reYAMF)

# reYAMF

A fork of YAMF (forked from [YAMFSquared](https://github.com/kaii-lb/YAMFsquared), actually), with my changes.

(project recreated. Check main-old branch to see previous commit by duzhaokun123 and kaii-lb)

## Requirements
- Android 13+ (>= api 33)
- LSPosed

## Features
- Launch from recents app icon
- Launch by home button long press (read further)
- Launch from app icon long press from homescreen/app drawer
- Launch from taskbar
- Multiple window support
- Minimize window to take up less space
- Resize window
- FLAG_SECURE support
- Quick Settings tiles 
- App Rotation detection
- Custom default window size
- Bunch of others in app :D

## My Changes
- Fix crash on Android 15
- Added sidebar. From app list window, long press app icon until the phone vibrate to add app to sidebar.
- Removed custom dpi, replace with auto density according to window size. Increase 'Reduce DPI' number from setting if you feel the app appearance is too big. Setting this value too big may cause crash (I recommended value between 50-100)

## Launch by home button long press (or swipe gesture)
- Be willing to give up google assistant
- Go to Settings -> Assistant App
- Change it to reYAMF
- Profit

## How to install
- Grab the latest APK from the [releases section](https://github.com/JuanArton/YAMFsquared/releases)
- Install it
- Enable module in LSPosed
- Go to Accessibility Settings and enable reYAMF accessibility service
- Reboot

## "API" 
- Broadcast `com.mja.reyamf.action.CURRENT_TO_WINDOW` to float the currently visible app
- Maybe more to come

## Issues
- The system will crash if the module is different from the injected version, its an xposed thing
- Some apps can't seem to launch in small windows
- Some apps scale abnormally at certain sizes
- Some app restart while being resized. (Will fix by adding lock DPI option)

## TODO
- Minimize to app icon (half done? long press minimize button to try)
- RtL support
- Lock window DPI option
- You tell me

## Special Thanks
- MASSIVE thanks to [duzhaokun123](https://github.com/duzhaokun123) and [kaii-lb](https://github.com/kaii-lb/YAMFsquared)

- [AOSP](https://source.android.com/)
- [EzXHelper](https://github.com/KyuubiRan/EzXHelper)
- [FlexboxLayout](https://github.com/google/flexbox-layout)
- [Hide-My-Applist](https://github.com/Dr-TSNG/Hide-My-Applist)
- [LSPosed](https://github.com/LSPosed/LSPosed)
- [Material](https://material.io/)
- [Mi-FreeForm](https://github.com/sunshine0523/Mi-FreeForm)
- [QAuxiliary](https://github.com/cinit/QAuxiliary)
- [ViewBindingUtil](https://github.com/matsudamper/ViewBindingUtil)
- [gson](https://github.com/google/gson)
- [xposed](https://forum.xda-developers.com/xposed)
