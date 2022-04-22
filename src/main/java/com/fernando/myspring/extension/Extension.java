package com.fernando.myspring.extension;

import com.fernando.myspring.applicationContext.impl.MyAnnotationConfigApplicationContext;
import com.fernando.myspring.bean.BeanDefinition;

import java.lang.reflect.Method;
import java.util.List;

public interface Extension {

    /**
     * 扫包 确定beanDefinition之前
     * @param context
     * @throws Exception
     */
    void doOperation0 (MyAnnotationConfigApplicationContext context) throws Exception;

    /**
     * 扫包之后 创建对象之前
     * @param context
     * @throws Exception
     */
    void doOperation1 (MyAnnotationConfigApplicationContext context) throws Exception;

    /**
     * 创建对象之后 代理之前
     * @param context
     * @throws Exception
     */
    void doOperation2 (MyAnnotationConfigApplicationContext context) throws Exception;

    /**
     * 代理之后 自动装载之前
     * @param context
     * @throws Exception
     */
    void doOperation3 (MyAnnotationConfigApplicationContext context) throws Exception;

    /**
     * 自动装载之后 注入配置类之前
     * @param context
     * @throws Exception
     */
    void doOperation4 (MyAnnotationConfigApplicationContext context) throws Exception;

    /**
     * applicationContext创建完成之后 非单例或者延迟加载的bean的创建、实例化、代理、初始化工作之前
     * @param context
     * @param beanDefinition
     * @throws Exception
     */
    void doOperation5 (MyAnnotationConfigApplicationContext context, BeanDefinition beanDefinition) throws Exception;

    /**
     * 非单例或延迟加载bean的实例化之后 代理之前
     * @param context
     * @param o
     * @throws Exception
     */
    void doOperation6 (MyAnnotationConfigApplicationContext context, Object o) throws Exception;

    /**
     * 非单例或延迟加载bean的代理之后 初始化之前
     * @param context
     * @param o
     * @throws Exception
     */
    void doOperation7 (MyAnnotationConfigApplicationContext context, Object o) throws Exception;

    /**
     * 非单例或延迟加载bean的初始化之后
     * @param context
     * @param o
     * @throws Exception
     */
    void doOperation8 (MyAnnotationConfigApplicationContext context, Object o) throws Exception;

    /**
     * 注入配置类之后
     * @param context
     * @throws Exception
     */
    void doOperation9 (MyAnnotationConfigApplicationContext context) throws Exception;

    /**
     * 代理过程中 组装目标方法和通知方法的map之前
     * @param context
     * @param methodBeProxy
     * @param before
     * @param beforeAspect
     * @param after
     * @param afterAspect
     * @param afterThrowing
     * @param throwingAspect
     * @param afterReturning
     * @param returningAspect
     * @throws Exception
     */
    void doOperationWhenProxy (MyAnnotationConfigApplicationContext context, Method methodBeProxy,
                               List<Method> before, List<Object> beforeAspect,
                               List<Method> after, List<Object> afterAspect,
                               List<Method> afterThrowing, List<Object> throwingAspect,
                               List<Method> afterReturning, List<Object> returningAspect) throws Exception;
}

