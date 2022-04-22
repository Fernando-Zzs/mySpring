package com.fernando.myspring.entity.IOC;

import com.fernando.myspring.annotation.IOC.Service;
import com.fernando.myspring.annotation.IOC.Value;
import lombok.Data;

@Data
@Service
public class StudentService {
    @Value("service...")
    private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
