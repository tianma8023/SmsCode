package com.github.tianma8023.smscode.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    // Singleton instance
    private static boolean sHasInstance;

    // System default system handler
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;

    private Context mContext;

    private CrashHandler(Context context) {
        sHasInstance = true;

        mContext = context;
        // get system default crash handler
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        // set system default crash handler
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static void init(Context context) {
        if (!sHasInstance) {
            new CrashHandler(context);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        XLog.d("CurrentThread: {}", Thread.currentThread());
        handleException(e);
        // let system handle uncaught exception if default crash handler exists.
        // otherwise kill app programmatically
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(t, e);
        } else {
            Process.killProcess(Process.myPid());
        }
    }

    private void handleException(Throwable e) {
        if (e == null) {
            return;
        }

        File crashLogDir = getCrashLogDir(mContext);
        if (crashLogDir == null) {
            return;
        }
        boolean exists;
        exists = crashLogDir.exists() || crashLogDir.mkdirs();

        if (exists) {
            final File crashLogFile = saveCrashLogToFile(crashLogDir, mContext, e);
            if (crashLogFile != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(mContext, "Log: " + crashLogFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                }).start();
                XLog.i("Save crash file {} succeed", crashLogFile.getName());
            } else {
                XLog.e("Save crash file failed");
            }
        }
    }

    private File saveCrashLogToFile(File crashDir, Context context, Throwable e) {
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-sss", Locale.getDefault());
        String time = sdf.format(new Date(timestamp));
        String crashFileName = "crash_" + time + ".log";
        File crashFile = new File(crashDir, crashFileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(crashFile);

            Map<String, String> deviceInfos = gatherDeviceInfos(context);
            String exceptionInfo = gatherExceptionInfo(e);

            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, String> entry : deviceInfos.entrySet()) {
                sb.append(entry.getKey())
                        .append(" = ")
                        .append(entry.getValue())
                        .append("\n");
            }
            sb.append("\n");

            sb.append(exceptionInfo);
            fos.write(sb.toString().getBytes());
            return crashFile;
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    // gather device info
    private Map<String, String> gatherDeviceInfos(Context context) {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageInfo(context.getPackageName(), 0);
            if (pkgInfo != null) {
                map.put("versionName", pkgInfo.versionName);
                map.put("versionCode", pkgInfo.versionCode + "");
            }
        } catch (PackageManager.NameNotFoundException e) {
            XLog.e("Error occurs when gathering package info", e);
        }

        Field[] buildFields = Build.class.getDeclaredFields();
        for (Field field : buildFields) {
            try {
                field.setAccessible(true);
                map.put(field.getName(), field.get(null).toString());
            } catch (IllegalAccessException e) {
                XLog.e("Error occurs when gathering device info", e);
            }
        }
        return map;
    }

    private String gatherExceptionInfo(Throwable t) {
        Throwable cause = t;
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();
        return writer.toString();
    }

    private File getCrashLogDir(Context context) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return context.getExternalFilesDir("crash");
        } else {
            return new File(context.getFilesDir(), "crash");
        }
    }
}
