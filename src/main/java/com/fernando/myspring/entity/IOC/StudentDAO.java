package com.fernando.myspring.entity.IOC;

import com.fernando.myspring.annotation.IOC.Lazy;
import com.fernando.myspring.annotation.IOC.Repository;
import com.fernando.myspring.annotation.IOC.Value;
import lombok.Data;

@Data
@Repository
@Lazy
public class StudentDAO {
    @Value("1500")
    private Integer Max_Connection;

    @Value("true")
    private boolean isDao;
}
