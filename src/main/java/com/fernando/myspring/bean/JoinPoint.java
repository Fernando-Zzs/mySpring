package com.fernando.myspring.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Type;

@Data
@AllArgsConstructor
public class JoinPoint { // JoinPoint主要用于保存目标方法的参数列表、方法名、方法返回值类型等信息

    private String methodName;

    private Object[] parameters;

    private Type returnType;
}
