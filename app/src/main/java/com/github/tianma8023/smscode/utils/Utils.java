package com.github.tianma8023.smscode.utils;

import android.content.Context;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.widget.Toast;

import com.github.tianma8023.smscode.R;

import java.util.Locale;

/**
 * Other Utils
 */
public class Utils {

    private Utils() {
    }

    public static void showWebPage(Context context, String url) {
        try {
            CustomTabsIntent cti = new CustomTabsIntent.Builder().build();
            cti.launchUrl(context, Uri.parse(url));
        } catch (Exception e) {
            Toast.makeText(context, R.string.browser_install_or_enable_prompt, Toast.LENGTH_SHORT).show();
        }
    }

    private static String getLanguagePath() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String result = "en";
        if ("zh".equals(language)) {
            if ("CN".equalsIgnoreCase(country)) {
                result = "zh-CN";
            } else if ("HK".equalsIgnoreCase(country) || "TW".equalsIgnoreCase(country)) {
                result = "zh-TW";
            }
        }
        return result;
    }

    public static String getProjectDocUrl(String docBaseUrl, String docPath) {
        return docBaseUrl + "/" + getLanguagePath() + "/" + docPath;
    }
}
