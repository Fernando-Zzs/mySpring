package com.fernando.myspring.entity.AOP;

import com.fernando.myspring.annotation.AOP.*;
import com.fernando.myspring.annotation.IOC.Component;
import com.fernando.myspring.annotation.IOC.Lazy;

@Aspect
@Component
public class AspectDemo {

    @Before("com.fernando.myspring.entity.AOP.Book.getBook(java.lang.Integer, java.lang.String)")
    public void logBefore() {
        System.out.println("this is before");
    }

    @After("com.fernando.myspring.entity.AOP.Book.getBook(java.lang.Integer, java.lang.String)")
    public void logAfter() {
        System.out.println("this is after");
    }

    @AfterThrowing("com.fernando.myspring.entity.AOP.Book.getBook(java.lang.Integer, java.lang.String)")
    public void logThrowing() {
        System.out.println("this is throwing");
    }

    @AfterReturning("com.fernando.myspring.entity.AOP.Book.getBook(java.lang.Integer, java.lang.String)")
    public void logReturning() {
        System.out.println("this is return");
    }
}
