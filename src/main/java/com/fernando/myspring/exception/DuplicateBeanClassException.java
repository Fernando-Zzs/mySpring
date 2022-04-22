package com.fernando.myspring.exception;

public class DuplicateBeanClassException extends Exception {
    private final Class<?> clazz;

    public DuplicateBeanClassException(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void printStackTrace() {
        System.out.println("发生beanClass重复异常：" + clazz.getName());
        super.printStackTrace();
    }
}
