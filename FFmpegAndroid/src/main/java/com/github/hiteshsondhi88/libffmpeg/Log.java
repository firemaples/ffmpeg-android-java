package com.github.hiteshsondhi88.libffmpeg;

@SuppressWarnings("unused")
public class Log {

    private static String TAG = FFmpeg.class.getSimpleName();
    private static boolean DEBUG = false;
    private static LogInterceptor logInterceptor;

    public static void setLogInterceptor(LogInterceptor logInterceptor) {
        Log.logInterceptor = logInterceptor;
    }

    static void setDEBUG(boolean DEBUG) {
        Log.DEBUG = DEBUG;
    }

    static void setTAG(String tag) {
        Log.TAG = tag;
    }

    static void d(Object obj) {
        if (logInterceptor != null) {
            logInterceptor.log("d", TAG, obj != null ? obj.toString() : null + "");
        }
        if (DEBUG) {
            android.util.Log.d(TAG, obj != null ? obj.toString() : null + "");
        }
    }

    static void e(Object obj) {
        if (logInterceptor != null) {
            logInterceptor.log("e", TAG, obj != null ? obj.toString() : null + "");
        }
        if (DEBUG) {
            android.util.Log.e(TAG, obj != null ? obj.toString() : null + "");
        }
    }

    static void w(Object obj) {
        if (logInterceptor != null) {
            logInterceptor.log("w", TAG, obj != null ? obj.toString() : null + "");
        }
        if (DEBUG) {
            android.util.Log.w(TAG, obj != null ? obj.toString() : null + "");
        }
    }

    static void i(Object obj) {
        if (logInterceptor != null) {
            logInterceptor.log("i", TAG, obj != null ? obj.toString() : null + "");
        }
        if (DEBUG) {
            android.util.Log.i(TAG, obj != null ? obj.toString() : null + "");
        }
    }

    static void v(Object obj) {
        if (logInterceptor != null) {
            logInterceptor.log("v", TAG, obj != null ? obj.toString() : null + "");
        }
        if (DEBUG) {
            android.util.Log.v(TAG, obj != null ? obj.toString() : null + "");
        }
    }

    static void e(Object obj, Throwable throwable) {
        if (logInterceptor != null) {
            logInterceptor.log("e", TAG, obj != null ? obj.toString() : null + "", throwable);
        }
        if (DEBUG) {
            android.util.Log.e(TAG, obj != null ? obj.toString() : null + "", throwable);
        }
    }

    static void e(Throwable throwable) {
        if (logInterceptor != null) {
            logInterceptor.log("e", TAG, "", throwable);
        }
        if (DEBUG) {
            android.util.Log.e(TAG, "", throwable);
        }
    }

    public interface LogInterceptor {
        void log(String level, String tag, String log, Throwable throwable);

        void log(String level, String tag, String log);
    }
}
