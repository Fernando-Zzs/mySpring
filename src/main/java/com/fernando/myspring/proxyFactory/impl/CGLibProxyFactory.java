package com.fernando.myspring.proxyFactory.impl;

import com.fernando.myspring.bean.ProxyMethod;
import com.fernando.myspring.proxyFactory.ProxyFactory;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Method;
import java.util.Map;

public class CGLibProxyFactory implements ProxyFactory {

    private final Object realObject;

    public CGLibProxyFactory(Object realObject){
        this.realObject = realObject;
    }

    @Override
    public Object getProxyInstance(Map<Method, ProxyMethod> method2ProxyMethod) {
        // 通过给enhancer类（代理子类）添加回调的方式 操作字节码
        Enhancer enhancer = new Enhancer();
        // 设置父类
        enhancer.setSuperclass(realObject.getClass());
        // 设置回调
        enhancer.setCallback((MethodInterceptor) (o, method, args, methodProxy) -> {
            Object result = null;
            ProxyMethod proxyMethod = method2ProxyMethod.get(method);
            if(proxyMethod != null){
                try {
                    invokeMethods(proxyMethod.getBeforeObject(), proxyMethod.getBeforeMethod(), method, args, null, null);
                    result = methodProxy.invoke(realObject, args);
                    invokeMethods(proxyMethod.getReturningObject(), proxyMethod.getReturningMethods(), method, args, null, result);
                } catch (Throwable throwable) {
                    if(proxyMethod.getThrowingMethods().size() == 0) throw throwable;
                    else invokeMethods(proxyMethod.getThrowingObject(), proxyMethod.getThrowingMethods(), method, args, throwable, null);
                } finally {
                    invokeMethods(proxyMethod.getAfterObject(), proxyMethod.getAfterMethods(), method, args, null, null);
                }
                return result;
            } else {
                return methodProxy.invoke(realObject, args);
            }
        });
        return enhancer.create();
    }
}
