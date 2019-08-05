package com.github.tianma8023.smscode.utils;

import android.content.Context;

import com.github.tianma8023.smscode.constant.PrefConst;

import static com.github.tianma8023.smscode.constant.PrefConst.CLEAR_CLIPBOARD;
import static com.github.tianma8023.smscode.constant.PrefConst.CLEAR_CLIPBOARD_DEFAULT;

/**
 * Shared preferences utils only for this App.
 */
public class SPUtils {

    private static final String FIRST_RUN_SINCE_V1 = "first_run_v1";
    // 是否已经对MIUI的"通知类短信"权限进行提示了
    private static final String SERVICE_SMS_PROMPT_SHOWN = "service_sms_prompt_shown";
    private static final String LAST_SMS_DATE = "last_sms_date";
    private static final String LAST_SMS_SENDER = "last_sms_sender";
    // 本地的版本号
    private static final String LOCAL_VERSION_CODE = "local_version_code";

    private static final String KEY_AUTO_INPUT_MODE_ACCESSIBILITY = "pref_auto_input_mode_accessibility";
    private static final boolean KEY_AUTO_INPUT_MODE_ACCESSIBILITY_DEFAULT = false;
    private static final String KEY_AUTO_INPUT_MODE_ROOT = "pref_auto_input_mode_root";
    private static final boolean KEY_AUTO_INPUT_MODE_ROOT_DEFAULT = false;

    private SPUtils() {

    }

    /**
     * 是否在自v1.0版本以来第一次运行
     */
    public static boolean isFirstRunSinceV1(Context context) {
        return PreferenceUtils.getBoolean(context, FIRST_RUN_SINCE_V1, true);
    }

    public static void setFirstRunSinceV1(Context context, boolean value) {
        PreferenceUtils.putBoolean(context, FIRST_RUN_SINCE_V1, value);
    }

    /**
     * MIUI的"通知类短信"权限申请是否已经提示过
     */
    public static boolean isServiceSmsPromptShown(Context context) {
        return PreferenceUtils.getBoolean(context, SERVICE_SMS_PROMPT_SHOWN, false);
    }

    /**
     * 设置MIUI的"通知类短信"权限申请是否已经提示过
     */
    public static void setServiceSmsPromptShown(Context context, boolean shown) {
        PreferenceUtils.putBoolean(context, SERVICE_SMS_PROMPT_SHOWN, shown);
    }

    /**
     * 记录上次SMS的Sender
     */
    public static void setLastSmsSender(Context context, String lastSender) {
        PreferenceUtils.putString(context, LAST_SMS_SENDER, lastSender);
    }

    /**
     * 获取上次SMS的Sender
     */
    public static String getLastSmsSender(Context context) {
        return PreferenceUtils.getString(context, LAST_SMS_SENDER, "");
    }

    /**
     * 设置上次SMS的Date
     */
    public static void setLastSmsDate(Context context, long lastSendDate) {
        PreferenceUtils.putLong(context, LAST_SMS_DATE, lastSendDate);
    }

    /**
     * 获取上次SMS的Date
     */
    public static long getLastSmsDate(Context context) {
        return PreferenceUtils.getLong(context, LAST_SMS_DATE, -1L);
    }

    /**
     * 总开关是否打开
     */
    public static boolean isEnable(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.ENABLE, PrefConst.ENABLE_DEFAULT);
    }

    /**
     * 获取短信监听模式
     */
    public static String getListenMode(Context context) {
        return PreferenceUtils.getString(context,
                PrefConst.LISTEN_MODE, PrefConst.LISTEN_MODE_STANDARD);
    }

    /**
     * 自动输入总开关是否打开
     */
    public static boolean autoInputCodeEnabled(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.ENABLE_AUTO_INPUT_CODE, PrefConst.ENABLE_AUTO_INPUT_CODE_DEFAULT);
    }

    /**
     * 自动输入模式是否是root模式(仅用于兼容之前版本)
     */
    public static boolean isAutoInputRootMode(Context context) {
        return PreferenceUtils.getBoolean(context,
                KEY_AUTO_INPUT_MODE_ROOT, KEY_AUTO_INPUT_MODE_ROOT_DEFAULT);
    }

    /**
     * 自动输入模式是否是无障碍模式(仅用于兼容之前版本)
     */
    public static boolean isAutoInputAccessibilityMode(Context context) {
        return PreferenceUtils.getBoolean(context,
                KEY_AUTO_INPUT_MODE_ACCESSIBILITY, KEY_AUTO_INPUT_MODE_ACCESSIBILITY_DEFAULT);
    }

    /**
     * 设置自动输入模式
     */
    public static void setAutoInputMode(Context context, String autoInputMode) {
        PreferenceUtils.putString(context,
                PrefConst.AUTO_INPUT_MODE, autoInputMode);
    }

    /**
     * 获取自动输入模式
     */
    public static String getAutoInputMode(Context context) {
        return PreferenceUtils.getString(context,
                PrefConst.AUTO_INPUT_MODE, PrefConst.AUTO_INPUT_MODE_DEFAULT);
    }

    /**
     * 日志模式是否是 Verbose log 模式
     */
    public static boolean isVerboseLogMode(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.VERBOSE_LOG_MODE, PrefConst.VERBOSE_LOG_MODE_DEFAULT);
    }

    /**
     * 复制到剪切板之后，是否显示toast
     */
    public static boolean showToast(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.SHOW_TOAST, PrefConst.SHOW_TOAST_DEFAULT);
    }

    /**
     * 获取对焦模式
     */
    public static String getFocusMode(Context context) {
        return PreferenceUtils.getString(context,
                PrefConst.FOCUS_MODE, PrefConst.FOCUS_MODE_MANUAL);
    }

    /**
     * 获取验证码短信关键字(正则表达式)
     */
    public static String getSMSCodeKeywords(Context context) {
        return PreferenceUtils.getString(context,
                PrefConst.SMSCODE_KEYWORDS, PrefConst.SMSCODE_KEYWORDS_DEFAULT);
    }

    /**
     * 获取当前主题的index
     */
    public static int getCurrentThemeIndex(Context context) {
        return PreferenceUtils.getInt(context,
                PrefConst.CURRENT_THEME_INDEX, PrefConst.CURRENT_THEME_INDEX_DEFAULT);
    }

    /**
     * 设置当前主题的index
     */
    public static void setCurrentThemeIndex(Context context, int curIndex) {
        PreferenceUtils.putInt(context, PrefConst.CURRENT_THEME_INDEX, curIndex);
    }

    /**
     * 获取本地记录的版本号
     */
    public static int getLocalVersionCode(Context context) {
        // 如果不存在,则默认返回2,即v1.0.1版本
        return PreferenceUtils.getInt(context,
                LOCAL_VERSION_CODE, 2);
    }

    /**
     * 设置当前版本号
     */
    public static void setLocalVersionCode(Context context, int versionCode) {
        PreferenceUtils.putInt(context, LOCAL_VERSION_CODE, versionCode);
    }

    /**
     * 是否应该在自动输入成功之后清理剪切板
     */
    public static boolean shouldClearClipboard(Context context) {
        return PreferenceUtils.getBoolean(context,
                CLEAR_CLIPBOARD, CLEAR_CLIPBOARD_DEFAULT);
    }

    /**
     * 标记为已读是否打开
     */
    public static boolean markAsReadEnabled(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.MARK_AS_READ, PrefConst.MARK_AS_READ_DEFAULT);
    }

    /**
     * 是否删除验证码短信
     */
    public static boolean deleteSmsEnabled(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.DELETE_SMS, PrefConst.DELETE_SMS_DEFAULT);
    }

    /**
     * 是否复制到剪切板
     */
    public static boolean copyToClipboardEnabled(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.COPY_TO_CLIPBOARD, PrefConst.COPY_TO_CLIPBOARD_DEFAULT);
    }

    /**
     * 是否在自动对焦失败后转为手动对焦
     */
    public static boolean manualFocusIfFailedEnabled(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.MANUAL_FOCUS_IF_FAILED, PrefConst.MANUAL_FOCUS_IF_FAILED_DEFAULT);
    }

    /**
     * 是否记录短信验证码
     */
    public static boolean recordSmsCodeEnabled(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.ENABLE_CODE_RECORDS, PrefConst.ENABLE_CODE_RECORDS_DEFAULT);
    }

    /**
     * 是否拦截短信通知
     */
    public static boolean blockNotificationEnabled(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.BLOCK_NOTIFICATION, PrefConst.BLOCK_NOTIFICATION_DEFAULT);
    }

    /**
     * 是否显示验证码通知
     */
    public static boolean showCodeNotification(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.SHOW_CODE_NOTIFICATION, PrefConst.SHOW_CODE_NOTIFICATION_DEFAULT);
    }

    /**
     * 是否自动清除验证码通知
     */
    public static boolean autoCancelCodeNotification(Context context) {
        return PreferenceUtils.getBoolean(context,
                PrefConst.AUTO_CANCEL_CODE_NOTIFICATION, PrefConst.AUTO_CANCEL_CODE_NOTIFICATION_DEFAULT);
    }

    /**
     * 获取验证码通知保留时间
     */
    public static int getNotificationRetentionTime(Context context) {
        String value = PreferenceUtils.getString(context,
                PrefConst.NOTIFICATION_RETENTION_TIME, PrefConst.NOTIFICATION_RETENTION_TIME_DEFAULT);
        return Integer.valueOf(value);
    }
}
