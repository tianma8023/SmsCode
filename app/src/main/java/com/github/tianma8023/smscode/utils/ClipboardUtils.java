package com.github.tianma8023.smscode.utils;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Clipboard utils.
 */
public class ClipboardUtils {

    private ClipboardUtils() {
    }

    public static void copyToClipboard(Context context, String text) {
        try {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm == null) {
                return;
            }
            ClipData clipData = ClipData.newPlainText("Copy text", text);
            cm.setPrimaryClip(clipData);
        } catch (Throwable t) {
            XLog.e("Error occurs when copy to clipboard", t);
        }
    }

    public static void clearClipboard(Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) {
            return;
        }
        if (cm.hasPrimaryClip()) {
            ClipDescription cd = cm.getPrimaryClipDescription();
            if (cd != null) {
                if (cd.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    cm.setPrimaryClip(ClipData.newPlainText("Copy text", ""));
                }
            }
        }
    }

}
