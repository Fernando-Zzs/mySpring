package com.fernando.myspring.entity.IOC;

import com.fernando.myspring.annotation.IOC.Component;
import com.fernando.myspring.annotation.IOC.Lazy;
import com.fernando.myspring.annotation.IOC.Scope;
import com.fernando.myspring.annotation.IOC.Value;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component("myOrder")
@Scope("prototype")
public class Order {
    @Value("xxx123")
    private String orderId;
    @Value("1000.5")
    private Float price;
}
