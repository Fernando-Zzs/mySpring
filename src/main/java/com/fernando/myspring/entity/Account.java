package com.fernando.myspring.entity;

import com.fernando.myspring.annotation.Autowired;
import com.fernando.myspring.annotation.Component;
import com.fernando.myspring.annotation.Qualifier;
import com.fernando.myspring.annotation.Value;
import lombok.Data;

@Data
@Component
public class Account {
    @Value("1")
    private Integer id;
    @Value("张三")
    private String name;
    @Value("22")
    private Integer age;
    @Autowired
    @Qualifier("myOrder")
    private Order order;
}
