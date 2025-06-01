package com.reliaquest.api.service;

import com.reliaquest.api.dto.*;
import com.reliaquest.api.exception.RateLimitExceededException;
import com.reliaquest.api.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeeServiceImplTest {

    @Mock
    private WebClient mockWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec mockRequestUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec mockRequestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec mockRequestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec mockRequestBodySpec;

    @Mock
    private WebClient.ResponseSpec mockResponseSpec;

    private EmployeeServiceImpl employeeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        employeeService = new EmployeeServiceImpl(mockWebClient);
    }

    // Tests for getAllEmployees()
    @Test
    public void testGetAllEmployees_nonEmpty() {
        ExternalEmployeeDto extDto = new ExternalEmployeeDto(
                "id1", "gajanan", 50000, 30,
                "engineer", "gajanan@example.com"
        );
        ResponseWrapperList<ExternalEmployeeDto> wrapper =
                new ResponseWrapperList<>(List.of(extDto), "OK");

        stubGetEmployeeResponse(Mono.just(wrapper));

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
        Employee emp = result.get(0);
        assertEquals("gajanan", emp.name());
        assertEquals(50000, emp.salary());
        verify(mockWebClient, times(1)).get();
    }

    @Test
    public void testGetAllEmployees_emptyList() {
        ResponseWrapperList<ExternalEmployeeDto> wrapper =
                new ResponseWrapperList<>(new ArrayList<>(), "OK");

        stubGetEmployeeResponse(Mono.just(wrapper));

        List<Employee> result = employeeService.getAllEmployees();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllEmployees_rateLimit() {
        stubGetEmployeeResponse(Mono.error(new RateLimitExceededException("429")));

        assertThrows(RateLimitExceededException.class, () -> employeeService.getAllEmployees());
    }

    @Test
    public void testGetAllEmployees_networkError() {
        stubGetEmployeeResponse(Mono.error(new RuntimeException("network")));

        assertThrows(RuntimeException.class, () -> employeeService.getAllEmployees());
    }

    // Tests for getEmployeeById()
    @Test
    public void testGetEmployeeById_found() {
        String id = "id1";
        ExternalEmployeeDto extDto = new ExternalEmployeeDto(
                id, "sachin", 60000, 28,
                "analyst", "sachin@example.com"
        );
        ResponseWrapperSingle<ExternalEmployeeDto> wrapper =
                new ResponseWrapperSingle<>(extDto, "OK");

        stubGetEmployeeByIdResponse(id, Mono.just(wrapper));

        Optional<Employee> result = employeeService.getEmployeeById(id);
        assertTrue(result.isPresent());
        assertEquals("sachin", result.get().name());
    }

    @Test
    public void testGetEmployeeById_notFound_nullData() {
        String id = "id2";
        ResponseWrapperSingle<ExternalEmployeeDto> wrapper =
                new ResponseWrapperSingle<>(null, "OK");

        stubGetEmployeeByIdResponse(id, Mono.just(wrapper));

        Optional<Employee> result = employeeService.getEmployeeById(id);
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetEmployeeById_rateLimit() {
        String id = "id3";
        stubGetEmployeeByIdResponse(id, Mono.error(new RateLimitExceededException("429")));

        assertThrows(RateLimitExceededException.class, () -> employeeService.getEmployeeById(id));
    }

    @Test
    public void testGetEmployeeById_networkError() {
        String id = "id4";
        stubGetEmployeeByIdResponse(id, Mono.error(new RuntimeException("fail")));

        assertThrows(RuntimeException.class, () -> employeeService.getEmployeeById(id));
    }

    // Tests for getEmployeesByNameSearch()
    @Test
    public void testGetEmployeesByNameSearch_matches() {
        ExternalEmployeeDto dto1 = new ExternalEmployeeDto("id1", "gajanan",
                50000, 30, "engineer", "gajanan@example.com");
        ExternalEmployeeDto dto2 = new ExternalEmployeeDto("id2", "sachin",
                60000, 28, "analyst", "sachin@example.com");
        ResponseWrapperList<ExternalEmployeeDto> wrapper = new ResponseWrapperList<>(List.of(dto1, dto2), "OK");

        stubGetEmployeeResponse(Mono.just(wrapper));

        List<Employee> result = employeeService.getEmployeesByNameSearch("gajanan");
        assertEquals(1, result.size());
        assertEquals("gajanan", result.get(0).name());
    }

    @Test
    public void testGetEmployeesByNameSearch_noMatches() {
        ExternalEmployeeDto dto1 = new ExternalEmployeeDto("id1", "gajanan",
                50000, 30, "engineer", "gajanan@example.com");
        ResponseWrapperList<ExternalEmployeeDto> wrapper = new ResponseWrapperList<>(List.of(dto1), "OK");

        stubGetEmployeeResponse(Mono.just(wrapper));

        List<Employee> result = employeeService.getEmployeesByNameSearch("xyz");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetEmployeesByNameSearch_rateLimit() {
        stubGetEmployeeResponse(Mono.error(new RateLimitExceededException("429")));

        assertThrows(RateLimitExceededException.class, () -> employeeService.getEmployeesByNameSearch("A"));
    }

    @Test
    public void testGetEmployeesByNameSearch_networkError() {
        stubGetEmployeeResponse(Mono.error(new RuntimeException("fail")));

        assertThrows(RuntimeException.class, () -> employeeService.getEmployeesByNameSearch("A"));
    }

    // Tests for getHighestSalaryOfEmployees()
    @Test
    public void testGetHighestSalaryOfEmployees_typical() {
        ExternalEmployeeDto dto1 = new ExternalEmployeeDto("id1", "gajanan",
                50000, 30, "engineer", "gajanan@example.com");
        ExternalEmployeeDto dto2 = new ExternalEmployeeDto("id2", "sachin",
                60000, 28, "analyst", "sachin@example.com");
        ResponseWrapperList<ExternalEmployeeDto> wrapper = new ResponseWrapperList<>(List.of(dto1, dto2), "OK");

        stubGetEmployeeResponse(Mono.just(wrapper));

        int highest = employeeService.getHighestSalaryOfEmployees();
        assertEquals(60000, highest);
    }

    @Test
    public void testGetHighestSalaryOfEmployees_single() {
        ExternalEmployeeDto dto1 = new ExternalEmployeeDto("id1", "gajanan",
                75000, 30, "engineer", "gajanan@example.com");
        ResponseWrapperList<ExternalEmployeeDto> wrapper = new ResponseWrapperList<>(List.of(dto1), "OK");

        stubGetEmployeeResponse(Mono.just(wrapper));

        int highest = employeeService.getHighestSalaryOfEmployees();
        assertEquals(75000, highest);
    }

    @Test
    public void testGetHighestSalaryOfEmployees_empty() {
        ResponseWrapperList<ExternalEmployeeDto> wrapper = new ResponseWrapperList<>(new ArrayList<>(), "OK");

        stubGetEmployeeResponse(Mono.just(wrapper));

        int highest = employeeService.getHighestSalaryOfEmployees();
        assertEquals(0, highest);
    }

    @Test
    public void testGetHighestSalaryOfEmployees_rateLimit() {
        stubGetEmployeeResponse(Mono.error(new RateLimitExceededException("429")));

        assertThrows(RateLimitExceededException.class, () -> employeeService.getHighestSalaryOfEmployees());
    }

    @Test
    public void testGetHighestSalaryOfEmployees_networkError() {
        stubGetEmployeeResponse(Mono.error(new RuntimeException("fail")));

        assertThrows(RuntimeException.class, () -> employeeService.getHighestSalaryOfEmployees());
    }

    // Tests for getTopTenHighestEarningEmployeeNames()
    @Test
    public void testGetTopTenHighestEarningEmployeeNames_moreThanTen() {
        List<ExternalEmployeeDto> dtoList = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            dtoList.add(new ExternalEmployeeDto(
                    "id" + i,
                    "Emp" + i,
                    i * 10000,
                    25 + i,
                    "Title",
                    "email" + i + "@x.com"
            ));
        }
        ResponseWrapperList<ExternalEmployeeDto> wrapper = new ResponseWrapperList<>(dtoList, "OK");

        stubGetEmployeeResponse(Mono.just(wrapper));

        List<String> names = employeeService.getTopTenHighestEarningEmployeeNames();
        assertEquals(10, names.size());
        assertEquals("Emp12", names.get(0));
        assertEquals("Emp3", names.get(9));
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames_fewerThanTen() {
        List<ExternalEmployeeDto> dtoList = List.of(
                new ExternalEmployeeDto("id1", "gajanan", 50000, 30,
                        "engineer", "gajanan@example.com"),
                new ExternalEmployeeDto("id2", "sachin", 60000, 28,
                        "analyst", "sachin@example.com")
        );
        ResponseWrapperList<ExternalEmployeeDto> wrapper = new ResponseWrapperList<>(dtoList, "OK");

        stubGetEmployeeResponse(Mono.just(wrapper));

        List<String> names = employeeService.getTopTenHighestEarningEmployeeNames();
        assertEquals(2, names.size());
        assertEquals("sachin", names.get(0));
        assertEquals("gajanan", names.get(1));
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames_exactlyTen() {
        List<ExternalEmployeeDto> dtoList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            dtoList.add(new ExternalEmployeeDto(
                    "id" + i,
                    "Emp" + i,
                    i * 1000,
                    25 + i,
                    "Title",
                    "email" + i + "@x.com"
            ));
        }
        ResponseWrapperList<ExternalEmployeeDto> wrapper = new ResponseWrapperList<>(dtoList, "OK");

        stubGetEmployeeResponse(Mono.just(wrapper));

        List<String> names = employeeService.getTopTenHighestEarningEmployeeNames();
        assertEquals(10, names.size());
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames_empty() {
        ResponseWrapperList<ExternalEmployeeDto> wrapper = new ResponseWrapperList<>(new ArrayList<>(), "OK");

        stubGetEmployeeResponse(Mono.just(wrapper));

        List<String> names = employeeService.getTopTenHighestEarningEmployeeNames();
        assertTrue(names.isEmpty());
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames_rateLimit() {
        stubGetEmployeeResponse(Mono.error(new RateLimitExceededException("429")));

        assertThrows(RateLimitExceededException.class, () -> employeeService.getTopTenHighestEarningEmployeeNames());
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames_networkError() {
        stubGetEmployeeResponse(Mono.error(new RuntimeException("fail")));

        assertThrows(RuntimeException.class, () -> employeeService.getTopTenHighestEarningEmployeeNames());
    }

    // Tests for createEmployee()
    @Test
    public void testCreateEmployee_success() {
        CreateEmployeeRequest request = new CreateEmployeeRequest("sandeep", 70000,
                35, "Manager");
        ExternalEmployeeDto extDto = new ExternalEmployeeDto(
                "id2", "sandeep", 70000, 35,
                "Manager", "sandeep@example.com"
        );
        ResponseWrapperSingle<ExternalEmployeeDto> wrapper =
                new ResponseWrapperSingle<>(extDto, "Created");

        stubCreateEmployeeResponse(Mono.just(wrapper));

        Optional<Employee> result = employeeService.createEmployee(request);
        assertTrue(result.isPresent());
        assertEquals("sandeep", result.get().name());
        assertEquals("id2", result.get().id());
    }

    @Test
    public void testCreateEmployee_nullData() {
        CreateEmployeeRequest request = new CreateEmployeeRequest("sandeep", 70000,
                35, "Manager");
        ResponseWrapperSingle<ExternalEmployeeDto> wrapper =
                new ResponseWrapperSingle<>(null, "OK");

        stubCreateEmployeeResponse(Mono.just(wrapper));

        Optional<Employee> result = employeeService.createEmployee(request);
        assertFalse(result.isPresent());
    }

    @Test
    public void testCreateEmployee_rateLimit() {
        CreateEmployeeRequest request = new CreateEmployeeRequest("sandeep", 70000,
                35, "Manager");

        stubCreateEmployeeResponse(Mono.error(new RateLimitExceededException("429")));

        assertThrows(RateLimitExceededException.class, () -> employeeService.createEmployee(request));
    }

    @Test
    public void testCreateEmployee_networkError() {
        CreateEmployeeRequest request = new CreateEmployeeRequest("sandeep", 70000,
                35, "Manager");

        stubCreateEmployeeResponse(Mono.error(new RuntimeException("fail")));

        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(request));
    }

    // Tests for deleteEmployeeByName()
    @Test
    public void testDeleteEmployeeByName_dataTrue() {
        String name = "jayesh";
        DeleteEmployeeRequest deleteDto = new DeleteEmployeeRequest(name);
        ResponseWrapperSingle<Boolean> wrapper =
                new ResponseWrapperSingle<>(true, "Deleted");

        stubDeleteEmployeeResponse(Mono.just(wrapper));

        boolean deleted = employeeService.deleteEmployeeByName(name);
        assertTrue(deleted);
    }

    @Test
    public void testDeleteEmployeeByName_dataFalse() {
        String name = "jayesh";
        DeleteEmployeeRequest deleteDto = new DeleteEmployeeRequest(name);
        ResponseWrapperSingle<Boolean> wrapper =
                new ResponseWrapperSingle<>(false, "OK");

        stubDeleteEmployeeResponse(Mono.just(wrapper));

        boolean deleted = employeeService.deleteEmployeeByName(name);
        assertFalse(deleted);
    }

    @Test
    public void testDeleteEmployeeByName_rateLimit() {
        String name = "jayesh";
        DeleteEmployeeRequest deleteDto = new DeleteEmployeeRequest(name);

        stubDeleteEmployeeResponse(Mono.error(new RateLimitExceededException("429")));

        assertThrows(RateLimitExceededException.class, () -> employeeService.deleteEmployeeByName(name));
    }

    @Test
    public void testDeleteEmployeeByName_networkError() {
        String name = "jayesh";
        DeleteEmployeeRequest deleteDto = new DeleteEmployeeRequest(name);

        stubDeleteEmployeeResponse(Mono.error(new RuntimeException("fail")));

        assertThrows(RuntimeException.class, () -> employeeService.deleteEmployeeByName(name));
    }

    private void stubGetEmployeeResponse(Mono<ResponseWrapperList<ExternalEmployeeDto>> wrapper) {
        when(mockWebClient.get()).thenReturn(mockRequestUriSpec);
        when(mockRequestUriSpec.uri("")).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(
                ArgumentMatchers.<ParameterizedTypeReference<ResponseWrapperList<ExternalEmployeeDto>>>any()
        )).thenReturn(wrapper);
    }

    private void stubGetEmployeeByIdResponse(String id, Mono<ResponseWrapperSingle<ExternalEmployeeDto>> wrapper) {
        when(mockWebClient.get()).thenReturn(mockRequestUriSpec);
        when(mockRequestUriSpec.uri("/{id}", id)).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(
                ArgumentMatchers.<ParameterizedTypeReference<ResponseWrapperSingle<ExternalEmployeeDto>>>any()
        )).thenReturn(wrapper);
    }

    private void stubCreateEmployeeResponse(Mono<ResponseWrapperSingle<ExternalEmployeeDto>> wrapper) {
        when(mockWebClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(mockRequestBodySpec);
        doReturn(mockRequestBodySpec)
                .when(mockRequestBodySpec)
                .body(any(Publisher.class), eq(CreateEmployeeRequest.class));
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(
                ArgumentMatchers.<ParameterizedTypeReference<ResponseWrapperSingle<ExternalEmployeeDto>>>any()
        )).thenReturn(wrapper);
    }

    private void stubDeleteEmployeeResponse(Mono<ResponseWrapperSingle<Boolean>> wrapper) {
        when(mockWebClient.method(org.springframework.http.HttpMethod.DELETE)).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(mockRequestBodySpec);
        doReturn(mockRequestBodySpec)
                .when(mockRequestBodySpec)
                .body(any(Publisher.class), eq(DeleteEmployeeRequest.class));
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(
                ArgumentMatchers.<ParameterizedTypeReference<ResponseWrapperSingle<Boolean>>>any()
        )).thenReturn(wrapper);
    }
}
