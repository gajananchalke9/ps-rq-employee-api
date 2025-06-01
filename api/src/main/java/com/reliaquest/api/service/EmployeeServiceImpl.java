package com.reliaquest.api.service;

import com.reliaquest.api.dto.*;
import com.reliaquest.api.exception.RateLimitExceededException;
import com.reliaquest.api.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final WebClient employeeWebClient;

    public EmployeeServiceImpl(WebClient employeeWebClient) {
        this.employeeWebClient = employeeWebClient;
    }

    @Override
    public List<Employee> getAllEmployees() {
        logger.info("Entering getAllEmployees()");
        try {

            Mono<ResponseWrapperList<ExternalEmployeeDto>> employeeMono = this.employeeWebClient
                    .get()
                    .uri("")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<>() {
                    });

            List<ExternalEmployeeDto> externalEmployeeList = employeeMono
                    .blockOptional()
                    .map(ResponseWrapperList::data)
                    .orElse(List.of());
            logger.info("Fetched {} employees from external service", externalEmployeeList.size());

            return externalEmployeeList.stream()
                    .map(this::mapToInternal)
                    .collect(Collectors.toList());
        } catch (RateLimitExceededException ex) {
            logger.error("Rate limit exceeded on getAllEmployees()", ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error in getAllEmployees()", ex);
            throw ex;
        }

    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        logger.info("Entering getEmployeesByNameSearch() with searchString='{}'", searchString);
        try {
            String lowerCaseSearchString = searchString.toLowerCase();

            return getAllEmployees().stream()
                    .filter(employee -> employee.name().toLowerCase().equals(lowerCaseSearchString))
                    .collect(Collectors.toList());
        } catch (RateLimitExceededException ex) {
            logger.error("Rate limit exceeded in getEmployeesByNameSearch()", ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error in getEmployeesByNameSearch()", ex);
            throw ex;
        }
    }

    @Override
    public Optional<Employee> getEmployeeById(String id) {
        logger.info("Entering getEmployeeById() with id='{}'", id);
        try {
            Mono<ResponseWrapperSingle<ExternalEmployeeDto>> responseMono = employeeWebClient
                    .get()
                    .uri("/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<>() {
                    });

            Optional<Employee> result = responseMono
                    .blockOptional()
                    .map(ResponseWrapperSingle::data)
                    .map(this::mapToInternal);
            logger.info("Fetched {} employee from external service", result);
            if (result.isPresent()) {
                logger.info("Employee found for id='{}'", id);
            } else {
                logger.info("No employee found for id='{}'", id);
            }
            return result;
        } catch (RateLimitExceededException ex) {
            logger.error("Rate limit exceeded in getEmployeeById('{}')", id, ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error in getEmployeeById('{}')", id, ex);
            throw ex;
        }
    }

    @Override
    public int getHighestSalaryOfEmployees() {
        logger.info("Entering getHighestSalaryOfEmployees()");
        try {

            return getAllEmployees().stream()
                    .map(Employee::salary)
                    .max(Comparator.naturalOrder())
                    .orElse(0);
        } catch (RateLimitExceededException ex) {
            logger.error("Rate limit exceeded in getHighestSalaryOfEmployees()", ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error in getHighestSalaryOfEmployees()", ex);
            throw ex;
        }
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        logger.info("Entering getTopTenHighestEarningEmployeeNames()");
        try {

            return getAllEmployees().stream()
                    .sorted(Comparator.comparingInt(Employee::salary).reversed())
                    .limit(10)
                    .map(Employee::name)
                    .collect(Collectors.toList());
        } catch (RateLimitExceededException ex) {
            logger.error("Rate limit exceeded in getTopTenHighestEarningEmployeeNames()", ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error in getTopTenHighestEarningEmployeeNames()", ex);
            throw ex;
        }
    }

    @Override
    public Optional<Employee> createEmployee(CreateEmployeeRequest request) {
        logger.info("Entering createEmployee() with request: name='{}', salary={}, age={}, title='{}'",
                request.name(), request.salary(), request.age(), request.title());
        try {

            Mono<ResponseWrapperSingle<ExternalEmployeeDto>> responseMono = employeeWebClient
                    .post()
                    .uri("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(request), CreateEmployeeRequest.class)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<>() {
                    });

            Optional<Employee> newEmployeeEntry = responseMono
                    .blockOptional()
                    .map(ResponseWrapperSingle::data)
                    .map(this::mapToInternal);

            if (newEmployeeEntry.isEmpty()) {
                logger.warn("createEmployee() returned no data");
            }
            return newEmployeeEntry;
        } catch (RateLimitExceededException ex) {
            logger.error("Rate limit exceeded in createEmployee('{}')", request.name(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error in createEmployee('{}')", request.name(), ex);
            throw ex;
        }
    }

    @Override
    public boolean deleteEmployeeByName(String name) {
        logger.info("Entering deleteEmployeeByName() with name='{}'", name);
        try {
            DeleteEmployeeRequest deleteEmployeeRequest = new DeleteEmployeeRequest(name);
            Mono<ResponseWrapperSingle<Boolean>> responseMono = employeeWebClient
                    .method(HttpMethod.DELETE)
                    .uri("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(deleteEmployeeRequest), DeleteEmployeeRequest.class)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<>() {
                    });

            boolean deleted = responseMono
                    .blockOptional()
                    .map(ResponseWrapperSingle::data)
                    .orElse(false);
            if (deleted) {
                logger.info("Successfully deleted employee with name='{}'", name);
            } else {
                logger.info("Employee with id='{}' not found or could not be deleted", name);
            }
            return deleted;
        } catch (RateLimitExceededException ex) {
            logger.error("Rate limit exceeded in deleteEmployeeByName('{}')", name, ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error in deleteEmployeeByName('{}')", name, ex);
            throw ex;
        }
    }

    private Employee mapToInternal(ExternalEmployeeDto dto) {
        logger.debug("Mapping ExternalEmployeeDto(id='{}') → Employee", dto.id());
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
