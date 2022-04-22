package com.fernando.myspring.proxyFactory;

import com.fernando.myspring.bean.JoinPoint;
import com.fernando.myspring.bean.ProxyMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface ProxyFactory {
    Object getProxyInstance(Map<Method, ProxyMethod> method2ProxyMethod);

    /**
     * 执行通知方法
     * @param aspect 切面类的实例化对象 用来执行方法
     * @param methods 真正执行的方法（代理前的方法）
     * @param realMethod 被代理的方法（公共代码逻辑）
     * @param realArgs 被代理的方法参数（公共代码逻辑）
     * @param t 被代理方法可能出现的异常
     * @param o 被代理方法执行的返回值
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    default void invokeMethods(List<Object> aspect, List<Method> methods, Method realMethod, Object[] realArgs, Throwable t, Object o) throws InvocationTargetException, IllegalAccessException {
        if(methods != null && methods.size() > 0){
            Iterator<Method> methodIterator = methods.iterator();
            Iterator<Object> objectIterator = aspect.iterator();
            while(methodIterator.hasNext() && objectIterator.hasNext()){
                Method method1 = methodIterator.next();
                Object object = objectIterator.next();
                Parameter[] parameters = method1.getParameters();
                Object[] args = new Object[parameters.length];
                for(int i = 0; i < parameters.length; i++){
                    if(parameters[i].getType().equals(JoinPoint.class)){
                        args[i] = new JoinPoint(realMethod.getName(), realArgs, realMethod.getReturnType());
                    } else if(parameters[i].getType().equals(Throwable.class)){
                        args[i] = t;
                    } else if(parameters[i].getType().equals(Object.class)){
                        args[i] = o;
                    } else if(parameters[i].getType().equals(Method.class)){
                        args[i] = realMethod;
                    }
                }
                method1.invoke(object, args);
            }
        }
    }
}
