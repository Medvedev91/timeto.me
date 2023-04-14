Google Play https://play.google.com/store/apps/details?id=app.time_to.timeto

App Store https://apps.apple.com/us/app/24-7-time-tracker-timeto/id1619782270

Any questions [hi@timeto.me](mailto:hi@timeto.me?subject=[GitHub]%20Feedback)

---

To build, you have to create  **local.properties** file like

```
sdk.dir=/Users/__USER__/Library/Android/sdk
```

---

# IntelliJ IDEA

`Settings -> Editor -> Code Style -> Kotlin -> Wrapping and Braces -> 'when' statements -> uncheck 'New line after multiline entry'`

`Settings -> Editor -> Code Style -> Kotlin -> Wrapping and Braces -> 'Hard wrap at:' 999`

`Settings -> Editor -> Code Style -> Kotlin -> Wrapping and Braces -> Binary expressions -> check 'Align when multiline'`

`Settings -> Editor -> TODO -> + -> Pattern: \btrick\b.*   Foreground: F22613`

# AppCode

`Settings -> Editor -> Code Style -> Swift -> Wrapping and Braces -> Binary expressions -> check 'Align when multiline'`

# Android Specific

### combinedClickable()

Based on https://stackoverflow.com/a/68744862
https://developer.android.com/jetpack/compose/gestures does not produce a ripple effect.

### AUTOSTART_TRIGGERS

Only explicit calls.

1. From tasks - check actions from the task text;
2. From timer - check actions from the activity name.

### ACTION_PERFORM_ERROR

Links can be taken from backup, there is no need to validate them.

*Finish Android Specific*

---

# iOS Specific

Disable landscape https://www.hackingwithswift.com/forums/swiftui/disable-rotation/9898

```
@NigelGee  HWS+
Go it the main and select TARGETS then select Info tab (the plist) and open Supported inferface orientations (iPhone) the click on the ones that you do not need. Just leave Portrait(bottom home button). That should make the UI stay one way.
```

---

### TruncationDynamic

https://stackoverflow.com/a/70461332

If the text fits in one line but such that you need two when adding more than one character,
then when you edit the object and add those few characters, the cell height is not updated.
Adding an id solves the problem.

Setting for Identifiable like this does not work:

```
var list_id: String {
    get {
        "\(id!.uuidString) + \(text!)"
    }
}
```

*Finish iOS Specific*

---
