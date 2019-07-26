package com.wdb.pdd.common.exception;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/4 0004
 * @描述
 */
public class MyException extends RuntimeException {

    private static final long serialVersionUID = 4970525926099074380L;

    public MyException() {
        super();
    }

    public MyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyException(String message) {
        super(message);
    }

    public MyException(Throwable cause) {
        super(cause);
    }
}
