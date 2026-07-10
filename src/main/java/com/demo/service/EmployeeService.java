package com.demo.service;

import com.demo.model.Employee;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    public List<Employee> getEmployees() {

        return List.of(
                new Employee(1,"Kiran","DevOps"),
                new Employee(2,"Rama","Cloud"),
                new Employee(3,"John","Platform")
        );
    }
}
