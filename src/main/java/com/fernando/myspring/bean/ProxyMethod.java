package com.fernando.myspring.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

@Data
@AllArgsConstructor
public class ProxyMethod {
    List<Method> beforeMethod = new LinkedList<>();
    List<Object> beforeObject = new LinkedList<>();

    List<Method> returningMethods = new LinkedList<>();
    List<Object> returningObject = new LinkedList<>();

    List<Method> throwingMethods = new LinkedList<>();
    List<Object> throwingObject = new LinkedList<>();

    List<Method> afterMethods = new LinkedList<>();
    List<Object> afterObject = new LinkedList<>();
}
