package org.example.service;

import org.example.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    Employee savedEmployee(Employee employee);

    List<Employee> getAllEmployees();

    Optional<Employee> getEmployeeById(Long id);

    Employee updateEmployee(Employee employee);

    void deleteEmployee(Long id);
}
