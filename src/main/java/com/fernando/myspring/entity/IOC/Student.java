package com.fernando.myspring.entity.IOC;

import com.fernando.myspring.annotation.IOC.Component;
import com.fernando.myspring.annotation.IOC.Scope;
import lombok.ToString;

@ToString
@Component
@Scope("prototype")
public class Student {
    private Integer id;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
