package com.fernando.myspring.proxyFactory.impl;

import com.fernando.myspring.bean.ProxyMethod;
import com.fernando.myspring.proxyFactory.ProxyFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class JDKProxyFactory implements ProxyFactory {

    private final Object realObject;

    public JDKProxyFactory(Object realObject) {
        this.realObject = realObject;
    }

    @Override
    public Object getProxyInstance(Map<Method, ProxyMethod> method2ProxyMethod) {
        // 返回一个proxy匿名类 传入类加载器、代理方法的接口、InvocationHandler的函数式接口
        return Proxy.newProxyInstance(realObject.getClass().getClassLoader(),
                realObject.getClass().getInterfaces(),
                (proxy, method, args) -> { // 此处实现InvocationHandler接口
                    Object result = null;
                    ProxyMethod proxyMethod = method2ProxyMethod.get(method);
                    if (proxyMethod != null) {
                        try {
                            invokeMethods(proxyMethod.getBeforeObject(), proxyMethod.getBeforeMethod(), method, args, null, null);
                            result = method.invoke(realObject, args);
                            invokeMethods(proxyMethod.getReturningObject(), proxyMethod.getReturningMethods(), method, args, null, result);
                        } catch (Throwable throwable) {
                            if (proxyMethod.getThrowingMethods().size() == 0) throw throwable;
                            else
                                invokeMethods(proxyMethod.getThrowingObject(), proxyMethod.getThrowingMethods(), method, args, throwable, null);
                        } finally {
                            invokeMethods(proxyMethod.getAfterObject(), proxyMethod.getAfterMethods(), method, args, null, null);
                        }
                    } else {
                        result = method.invoke(realObject, args);
                    }
                    return result;
                });
    }
}
