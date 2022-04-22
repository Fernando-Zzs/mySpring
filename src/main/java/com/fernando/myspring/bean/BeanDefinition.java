package com.fernando.myspring.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BeanDefinition {
    private String beanName;
    private Class beanClass;
    private boolean lazy;
    private boolean singleton;
}
