package com.reliaquest.api.controller;

import com.reliaquest.api.dto.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/employees")
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeRequest> {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        logger.info("GET employees called");
        List<Employee> employees = employeeService.getAllEmployees();
        logger.info("GET employees returning {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        logger.info("GET employees/search/{} called", searchString);
        List<Employee> filteredEmployees = employeeService.getEmployeesByNameSearch(searchString);
        logger.info("GET employees/search/{} returning {} results", searchString, filteredEmployees.size());
        return ResponseEntity.ok(filteredEmployees);
    }

    @Override
    public ResponseEntity getEmployeeById(String id) {
        logger.info("GET employees/{} called", id);
        return employeeService.getEmployeeById(id)
                .map(emp -> {
                    logger.info("Employee found with id='{}'", id);
                    return ResponseEntity.ok(emp);
                })
                .orElseGet(() -> {
                    logger.info("Employee not found with id='{}'", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        logger.info("GET employees/highestSalary called");
        int highestSalary = employeeService.getHighestSalaryOfEmployees();
        logger.info("GET employees/highestSalary returning {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        logger.info("GET employees/topTenHighestEarningEmployeeNames called");
        List<String> highestEarningEmployeeNames = employeeService.getTopTenHighestEarningEmployeeNames();
        logger.info("GET employees/topTenHighestEarningEmployeeNames returning {} names", highestEarningEmployeeNames.size());
        return ResponseEntity.ok(highestEarningEmployeeNames);
    }

    @Override
    @PostMapping
    public ResponseEntity createEmployee(CreateEmployeeRequest employeeInput) {
        logger.info("POST employees called with payload: name='{}', salary={}, age={}, title='{}'",
                employeeInput.name(), employeeInput.salary(), employeeInput.age(), employeeInput.title());

        return employeeService.createEmployee(employeeInput)
                .map(created -> {
                    logger.info("Employee created successfully with id='{}'", created.id());
                    return ResponseEntity.ok(created);
                })
                .orElseGet(() -> {
                    logger.warn("createEmployee() returned empty result; sending 400 Bad Request");
                    return ResponseEntity.badRequest().build();
                });
    }


    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        logger.info("DELETE employees/{} called", id);
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        if (employee.isPresent()) {
            boolean deleted = employeeService.deleteEmployeeByName(employee.get().name());
            if (deleted) {
                logger.info("Employee with id='{}' deleted successfully", id);
                return ResponseEntity.ok("Employee deleted successfully");
            } else {
                logger.info("Employee with id='{}' not found or could not be deleted", id);
                return ResponseEntity.status(404).body("Employee not found or could not be deleted");
            }
        } else{
            logger.info("Employee not found with id='{}'", id);
            return ResponseEntity.status(404).body("Employee not found");
        }
    }
}
