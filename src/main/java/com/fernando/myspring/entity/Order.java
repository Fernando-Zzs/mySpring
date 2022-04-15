package com.fernando.myspring.entity;

import com.fernando.myspring.annotation.Component;
import com.fernando.myspring.annotation.Value;
import lombok.Data;

@Data
@Component("myOrder")
public class Order {
    @Value("xxx123")
    private String orderId;
    @Value("1000.5")
    private Float price;
}
