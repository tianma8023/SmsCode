package com.github.tianma8023.smscode.utils.rom;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Utils for check Chinese ROM.
 */
public class RomUtils {

    private RomUtils() {

    }

    public static boolean isMiui() {
        return !TextUtils.isEmpty(getSystemProperty(MiuiUtils.KEY_VERSION_NAME_MIUI));
    }

    public static String getSystemProperty(String propName) {
        String result = null;
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            result = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
