package com.sh.jerry.context;

/**
 * Created by jerry on 6/6/16.
 */
public class DBContext {

    private static final ThreadLocal<String> tlDbKey = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> isWrite = new ThreadLocal<>();

    public static Boolean isWriteMode() {
        return isWrite.get() == null ? false : isWrite.get();
    }

    public static void setIsWrite(Boolean value) {
        isWrite.set(value);
    }

    public static String getDBKey() {
        return tlDbKey.get();
    }

    public static void setDBKey(String dbKey) {
        tlDbKey.set(dbKey);
    }


}
