package com.fernando.myspring.exception;

public class DataConversionException extends Exception{
    private final Object val1;
    private final Object val2;

    public DataConversionException(Object val1, Object val2){
        System.err.println("发生数据转换异常：" + val1 + "->" + val2);
        this.val1 = val1;
        this.val2 = val2;
    }

    @Override
    public void printStackTrace() {
        System.err.println("发生数据转换异常：" + val1 + "->" + val2);
    }
}
