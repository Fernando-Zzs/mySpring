package com.fernando.myspring.entity.IOC;

import com.fernando.myspring.annotation.Bean;
import com.fernando.myspring.annotation.Configuration;

import java.awt.print.Book;

@Configuration(proxyBeanMethods = true)
public class MyConfig {
    @Bean(name = "ordererer")
    public Order order1(){
        return new Order("xxxx1234", 33f);
    }
}
