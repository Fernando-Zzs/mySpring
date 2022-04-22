package com.fernando.myspring.exception;

public class DuplicateBeanNameException extends Exception {
    private final String beanName;

    public DuplicateBeanNameException(String beanName) {
        System.err.println("发生beanName重复异常：" + beanName);
        this.beanName = beanName;
    }

    @Override
    public void printStackTrace() {
        System.err.println("发生beanName重复异常：" + beanName);
        super.printStackTrace();
    }
}
