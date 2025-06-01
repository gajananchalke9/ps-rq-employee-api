package com.reliaquest.api.service;

import com.reliaquest.api.dto.*;
import com.reliaquest.api.model.Employee;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final WebClient employeeWebClient;

    public EmployeeServiceImpl(WebClient employeeWebClient) {
        this.employeeWebClient = employeeWebClient;
    }

    @Override
    public List<Employee> getAllEmployees() {
        Mono<ResponseWrapperList<ExternalEmployeeDto>> employeeMono = this.employeeWebClient
                .get()
                .uri("")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});

        List<ExternalEmployeeDto> externalEmployeeList = employeeMono
                .blockOptional()
                .map(ResponseWrapperList::data)
                .orElse(List.of());

        return externalEmployeeList.stream()
                .map(this::mapToInternal)
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        String lowerCaseSearchString = searchString.toLowerCase();

        return getAllEmployees().stream()
                .filter(employee -> employee.name().toLowerCase().equals(lowerCaseSearchString))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Employee> getEmployeeById(String id) {
        Mono<ResponseWrapperSingle<ExternalEmployeeDto>> responseMono = employeeWebClient
                .get()
                .uri("/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});

        return responseMono
                .blockOptional()
                .map(ResponseWrapperSingle::data)
                .map(this::mapToInternal);
    }

    @Override
    public int getHighestSalaryOfEmployees() {
        return getAllEmployees().stream()
                .map(Employee::salary)
                .max(Comparator.naturalOrder())
                .orElse(0);
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        return getAllEmployees().stream()
                .sorted(Comparator.comparingInt(Employee::salary).reversed())
                .limit(10)
                .map(Employee::name)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Employee> createEmployee(CreateEmployeeRequest request) {
        Mono<ResponseWrapperSingle<ExternalEmployeeDto>> responseMono = employeeWebClient
                .post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), CreateEmployeeRequest.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});

        return responseMono
                .blockOptional()
                .map(ResponseWrapperSingle::data)
                .map(this::mapToInternal);
    }

    @Override
    public boolean deleteEmployeeById(String idOrName) {
        DeleteEmployeeRequest deleteEmployeeRequest = new DeleteEmployeeRequest(idOrName);
        Mono<ResponseWrapperSingle<Boolean>> responseMono = employeeWebClient
                .method(HttpMethod.DELETE)
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(deleteEmployeeRequest), DeleteEmployeeRequest.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});

        return responseMono
                .blockOptional()
                .map(ResponseWrapperSingle::data)
                .orElse(false);
    }

    private Employee mapToInternal(ExternalEmployeeDto dto) {
        return new Employee(
                dto.id(),
                dto.employeeName(),
                dto.employeeSalary(),
                dto.employeeAge(),
                dto.employeeTitle(),
                dto.employeeEmail()
        );
    }
}
