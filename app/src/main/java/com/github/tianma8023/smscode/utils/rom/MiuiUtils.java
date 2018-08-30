package com.github.tianma8023.smscode.utils.rom;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;

import com.github.tianma8023.smscode.utils.XLog;

import java.util.List;

public class MiuiUtils {

    public static final String KEY_VERSION_NAME_MIUI = "ro.miui.ui.version.name";

    private MiuiUtils() {

    }

    /**
     * 获取MIUI版本，失败则返回-1
     *
     * @return miui version code, return -1 if failed.
     */
    private static int getMiuiVersion() {
        String version = RomUtils.getSystemProperty(KEY_VERSION_NAME_MIUI);
        if (!TextUtils.isEmpty(version)) {
            try {
                return Integer.parseInt(version.substring(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private static boolean isActivityIntentValid(Context context, Intent intent) {
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> matchedActivities =  context.getPackageManager().
                queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return matchedActivities != null && !matchedActivities.isEmpty();
    }

    public static void goToPermissionEditorActivity(Context context) {
        int miuiVer = getMiuiVersion();
        Intent intent;
        if (miuiVer >= 8) { // miui v8, 9, 10
            intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", context.getPackageName());
        } else if (miuiVer >= 6) { // miui v6,7
            intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intent.putExtra("extra_pkgname", context.getPackageName());
        } else { // other version, 应用信息界面
            String packageName = context.getPackageName();
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package" , packageName, null);
            intent.setData(uri);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isActivityIntentValid(context, intent)) {
            context.startActivity(intent);
        } else {
            XLog.e("Intent is invalid {}", intent);
        }
    }

}
