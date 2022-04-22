package com.fernando.myspring.entity.AOP;

import com.fernando.myspring.annotation.IOC.Component;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Book {
    private Integer id;
    private String name;

    public void getBook(Integer id, String name){
        System.out.println("执行目标方法..." + id + name);
    }
}
