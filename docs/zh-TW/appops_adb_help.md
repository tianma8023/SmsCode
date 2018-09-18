簡訊權限 ADB 設定幫助
--------

- 權限說明
  
  自 Android 4.4 起，非默認簡訊應用修改簡訊受到限制。除了需要授予寫簡訊權限外，還需要修改 `AppOps` 的設置給予 `WRITE_SMS` 權限。

- ADB 設定
  1. 在電腦上配置 ADB 環境；
  2. 打開手機的 `開發者選項` 和 `USB偵錯`，并連接至電腦；
  3. 執行 ADB 命令：
  ```shell
  adb shell appops set com.github.tianma8023.smscode WRITE_SMS allow
  ```
  4. 重新打開應用中的 `標記為已讀`，檢查是否已經設定成功。

