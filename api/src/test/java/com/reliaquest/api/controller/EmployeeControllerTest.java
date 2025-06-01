package com.reliaquest.api.controller;


import com.reliaquest.api.dto.CreateEmployeeRequest;
import com.reliaquest.api.exception.RateLimitExceededException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // GET /employees
    @Test
    public void testGetAllEmployees_nonEmpty() throws Exception {
        Employee e1 = new Employee(
                "id1", "Gajanan", 50000, 30, "Engineer", "gajanan@example.com"
        );
        when(employeeService.getAllEmployees()).thenReturn(List.of(e1));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gajanan"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testGetAllEmployees_empty() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testGetAllEmployees_rateLimit() throws Exception {
        when(employeeService.getAllEmployees())
                .thenThrow(new RateLimitExceededException("429"));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("429"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testGetAllEmployees_genericError() throws Exception {
        when(employeeService.getAllEmployees())
                .thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getAllEmployees();
    }

    // GET /employees/search/{searchString}
    @Test
    public void testGetEmployeesByNameSearch_matches() throws Exception {
        Employee e = new Employee(
                "id1", "Sachin", 50000, 30, "Analyst", "sachin@example.com"
        );
        when(employeeService.getEmployeesByNameSearch("Sachin"))
                .thenReturn(List.of(e));

        mockMvc.perform(get("/employees/search/{searchString}", "Sachin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sachin"));

        verify(employeeService, times(1)).getEmployeesByNameSearch("Sachin");
    }

    @Test
    public void testGetEmployeesByNameSearch_noMatches() throws Exception {
        when(employeeService.getEmployeesByNameSearch("xyz")).thenReturn(List.of());

        mockMvc.perform(get("/employees/search/{searchString}", "xyz"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));

        verify(employeeService, times(1)).getEmployeesByNameSearch("xyz");
    }

    @Test
    public void testGetEmployeesByNameSearch_rateLimit() throws Exception {
        when(employeeService.getEmployeesByNameSearch("Sachin"))
                .thenThrow(new RateLimitExceededException("429"));

        mockMvc.perform(get("/employees/search/{searchString}", "Sachin"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("429"));

        verify(employeeService, times(1)).getEmployeesByNameSearch("Sachin");
    }

    @Test
    public void testGetEmployeesByNameSearch_genericError() throws Exception {
        when(employeeService.getEmployeesByNameSearch("Sachin"))
                .thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/employees/search/{searchString}", "Sachin"))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getEmployeesByNameSearch("Sachin");
    }

    // GET /employees/{id}
    @Test
    public void testGetEmployeeById_found() throws Exception {
        String id = "id1";
        Employee e = new Employee(
                id, "Sandeep", 60000, 28, "Analyst", "sandeep@example.com"
        );
        when(employeeService.getEmployeeById(id))
                .thenReturn(Optional.of(e));

        mockMvc.perform(get("/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sandeep"));

        verify(employeeService, times(1)).getEmployeeById(id);
    }

    @Test
    public void testGetEmployeeById_notFound() throws Exception {
        String id = "id2";
        when(employeeService.getEmployeeById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/employees/{id}", id))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(id);
    }

    @Test
    public void testGetEmployeeById_rateLimit() throws Exception {
        String id = "id3";
        when(employeeService.getEmployeeById(id))
                .thenThrow(new RateLimitExceededException("429"));

        mockMvc.perform(get("/employees/{id}", id))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("429"));

        verify(employeeService, times(1)).getEmployeeById(id);
    }

    @Test
    public void testGetEmployeeById_genericError() throws Exception {
        String id = "id4";
        when(employeeService.getEmployeeById(id))
                .thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/employees/{id}", id))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getEmployeeById(id);
    }

    // GET /employees/highestSalary

    @Test
    public void testGetHighestSalaryOfEmployees_positive() throws Exception {
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(80000);

        mockMvc.perform(get("/employees/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("80000"));

        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    @Test
    public void testGetHighestSalaryOfEmployees_zero() throws Exception {
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(0);

        mockMvc.perform(get("/employees/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    @Test
    public void testGetHighestSalaryOfEmployees_rateLimit() throws Exception {
        when(employeeService.getHighestSalaryOfEmployees())
                .thenThrow(new RateLimitExceededException("429"));

        mockMvc.perform(get("/employees/highestSalary"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("429"));

        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    @Test
    public void testGetHighestSalaryOfEmployees_genericError() throws Exception {
        when(employeeService.getHighestSalaryOfEmployees())
                .thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/employees/highestSalary"))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    // GET /employees/topTenHighestEarningEmployeeNames

    @Test
    public void testGetTopTenHighestEarningEmployeeNames_nonEmpty() throws Exception {
        when(employeeService.getTopTenHighestEarningEmployeeNames())
                .thenReturn(List.of("Gajanan", "Sandeep"));

        mockMvc.perform(get("/employees/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Gajanan"))
                .andExpect(jsonPath("$[1]").value("Sandeep"));

        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames_empty() throws Exception {
        when(employeeService.getTopTenHighestEarningEmployeeNames())
                .thenReturn(List.of());

        mockMvc.perform(get("/employees/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));

        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames_rateLimit() throws Exception {
        when(employeeService.getTopTenHighestEarningEmployeeNames())
                .thenThrow(new RateLimitExceededException("429"));

        mockMvc.perform(get("/employees/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("429"));

        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames_genericError() throws Exception {
        when(employeeService.getTopTenHighestEarningEmployeeNames())
                .thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/employees/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    // POST /employees

    @Test
    public void testCreateEmployee_success() throws Exception {
        CreateEmployeeRequest req = new CreateEmployeeRequest("Manoj", 70000, 35, "Manager");
        Employee e = new Employee("id2", "Manoj", 70000, 35, "Manager", "manoj@example.com");
        when(employeeService.createEmployee(req)).thenReturn(Optional.of(e));

        String json = """
                {
                  "name": "Manoj",
                  "salary": 70000,
                  "age": 35,
                  "title": "Manager"
                }
                """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id2"))
                .andExpect(jsonPath("$.name").value("Manoj"));

        verify(employeeService, times(1)).createEmployee(req);
    }

    @Test
    public void testCreateEmployee_validationFailure() throws Exception {
        // This JSON fails @NotBlank/@Min validations → 400
        String invalidJson = """
                {
                  "name": "",
                  "salary": 0,
                  "age": 0,
                  "title": ""
                }
                """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployee_serviceReturnsEmpty() throws Exception {
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenReturn(Optional.empty());

        String json = """
                {
                  "name": "Manoj",
                  "salary": 70000,
                  "age": 35,
                  "title": "Manager"
                }
                """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployee_rateLimit() throws Exception {
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenThrow(new RateLimitExceededException("429"));

        String json = """
                {
                  "name": "Manoj",
                  "salary": 70000,
                  "age": 35,
                  "title": "Manager"
                }
                """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("429"));
    }

    @Test
    public void testCreateEmployee_genericError() throws Exception {
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenThrow(new RuntimeException("fail"));

        String json = """
                {
                  "name": "Manoj",
                  "salary": 70000,
                  "age": 35,
                  "title": "Manager"
                }
                """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }

    // DELETE /employees/{id}
    @Test
    public void testDeleteEmployee_success() throws Exception {
        String id = "id1";
        Employee e = new Employee(id, "Sandeep", 60000, 28,
                "Analyst", "sandeep@example.com");
        when(employeeService.getEmployeeById("id1"))
                .thenReturn(Optional.of(e));
        when(employeeService.deleteEmployeeByName(e.name()))
                .thenReturn(true);

        mockMvc.perform(delete("/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee deleted successfully"));

        verify(employeeService, times(1)).deleteEmployeeByName(e.name());
    }

    @Test
    public void testDeleteEmployee_notFound() throws Exception {
        String id = "id1";
        Employee e = new Employee(id, "Sandeep", 60000, 28,
                "Analyst", "sandeep@example.com");
        when(employeeService.getEmployeeById("id1"))
                .thenReturn(Optional.of(e));
        when(employeeService.deleteEmployeeByName(e.name())).thenReturn(false);

        mockMvc.perform(delete("/employees/{id}", id))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).deleteEmployeeByName(e.name());
    }

    @Test
    public void testDeleteEmployee_rateLimit() throws Exception {
        String id = "id1";
        Employee e = new Employee(id, "Sandeep", 60000, 28,
                "Analyst", "sandeep@example.com");
        when(employeeService.getEmployeeById(id))
                .thenReturn(Optional.of(e));
        when(employeeService.deleteEmployeeByName(e.name()))
                .thenThrow(new RateLimitExceededException("429"));

        mockMvc.perform(delete("/employees/{id}", id))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("429"));

        verify(employeeService, times(1)).deleteEmployeeByName(e.name());
    }

    @Test
    public void testDeleteEmployee_genericError() throws Exception {
        String id = "id1";
        Employee e = new Employee(id, "Sandeep", 60000, 28,
                "Analyst", "sandeep@example.com");
        when(employeeService.getEmployeeById(id))
                .thenReturn(Optional.of(e));
        when(employeeService.deleteEmployeeByName(e.name()))
                .thenThrow(new RuntimeException("fail"));

        mockMvc.perform(delete("/employees/{id}", id))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).deleteEmployeeByName(e.name());
    }
}
