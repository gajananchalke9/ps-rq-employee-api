package com.reliaquest.api.service;

import com.reliaquest.api.dto.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    List<Employee> getAllEmployees();

    List<Employee> getEmployeesByNameSearch(String searchString);

    Optional<Employee> getEmployeeById(String id);

    int getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();

    Optional<Employee> createEmployee(CreateEmployeeRequest request);

    boolean deleteEmployeeByName(String name);
}
