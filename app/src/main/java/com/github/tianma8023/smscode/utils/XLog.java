package com.github.tianma8023.smscode.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


public class XLog {

    private static Logger sLogger;

    private XLog() {
    }

    public static void init(Context context) {
        if (sLogger == null) {
            initLogger(context);
        }
    }

    private static void initLogger(Context context) {
        sLogger = (Logger) LoggerFactory.getLogger("SmsCodeLogger");
        boolean isVerboseLogMode = SPUtils.isVerboseLogMode(context);
        if (isVerboseLogMode) {
            setLogLevel(Level.TRACE);
        } else {
            setLogLevel(Level.INFO);
        }
    }

    public static void v(String message, Object... args) {
        sLogger.trace(message, args);
    }

    public static void d(String message, Object... args) {
        sLogger.debug(message, args);
    }

    public static void i(String message, Object... args) {
        sLogger.info(message, args);
    }

    public static void w(String message, Object... args) {
        sLogger.info(message, args);
    }

    public static void e(String message, Object... args) {
        sLogger.error(message, args);
    }

    public static void e(String message, Throwable e) {
        sLogger.error(message, e);
    }

    public static void setLogLevel(@NonNull Level level) {
        Level curLevel = sLogger.getLevel();
        if (curLevel.toInt() != level.toInt()) {
            sLogger.setLevel(level);
        }
    }
}
