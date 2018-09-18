短信权限 ADB 设置帮助
--------

- 权限说明
  
  自 Android 4.4 起，非默认短信应用修改短信受到限制。除了需要授予写短信权限外，还需要修改 `AppOps` 的设置给予 `WRITE_SMS` 权限。

- ADB 设置
  1. 在电脑上配置ADB环境；
  2. 打开手机 `开发者选项` 和 `USB调试`，并连接电脑；
  3. 执行 ADB 命令：
  ```shell
  adb shell appops set com.github.tianma8023.smscode WRITE_SMS allow
  ```
  4. 重新打开`标记为已读`开关，检查是否已经设置成功。

