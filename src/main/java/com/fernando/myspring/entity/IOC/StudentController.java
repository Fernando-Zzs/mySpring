package com.fernando.myspring.entity.IOC;

import com.fernando.myspring.annotation.IOC.Autowired;
import com.fernando.myspring.annotation.IOC.Controller;
import lombok.ToString;

@ToString
@Controller("myStudentController")
public class StudentController {
    private String id;

    @Autowired
    private StudentService studentService;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
