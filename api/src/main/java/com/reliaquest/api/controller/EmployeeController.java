package com.reliaquest.api.controller;

import com.reliaquest.api.dto.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeRequest> {

    private EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        List<Employee> filteredEmployees = employeeService.getEmployeesByNameSearch(searchString);
        return ResponseEntity.ok(filteredEmployees);
    }

    @Override
    public ResponseEntity getEmployeeById(String id) {
        return employeeService.getEmployeeById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        int  highestSalary = employeeService.getHighestSalaryOfEmployees();
        return ResponseEntity.ok(highestSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> highestEarningEmployeeNames = employeeService.getTopTenHighestEarningEmployeeNames();
        return ResponseEntity.ok(highestEarningEmployeeNames);
    }

    @Override
    @PostMapping
    public ResponseEntity createEmployee(CreateEmployeeRequest employeeInput) {
        return employeeService.createEmployee( employeeInput)
                .map(created -> ResponseEntity.ok(created))
                .orElse(ResponseEntity.badRequest().build());
    }


    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        boolean deleted = employeeService.deleteEmployeeById(id);
        if (deleted) {
            return ResponseEntity.ok("Employee deleted successfully");
        } else {
            return ResponseEntity.status(404).body("Employee not found or could not be deleted");
        }
    }
}
