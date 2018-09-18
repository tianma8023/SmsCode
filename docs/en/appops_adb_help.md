AppOpsManager ADB setting help
--------

- Permission statement
  
  Writing SMS for Non-default SMS app has been limited since Android 4.4. Except for granting write SMS runtime permission, WRITE_SMS permission in AppOpsManager also should be granted.

- ADB settings
  1. Install ADB environment in your computer;
  2. Open "Developer options" and "USB debugging" on your phone and then link to the computer.
  3. Execute ADB command:
  ```shell
  adb shell appops set com.github.tianma8023.smscode WRITE_SMS allow
  ```
  4. Reopen "Mark as read" switch to check whether permission is granted or not.

