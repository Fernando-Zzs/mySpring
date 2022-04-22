package com.fernando.myspring.entity.IOC;

import com.fernando.myspring.annotation.IOC.Autowired;
import com.fernando.myspring.annotation.IOC.Component;
import com.fernando.myspring.annotation.IOC.Qualifier;
import com.fernando.myspring.annotation.IOC.Value;
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
