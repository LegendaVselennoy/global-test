package org.example.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.EmployeeController;
import org.example.entity.Employee;
import org.example.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EmployeeService employeeService;
    @Autowired
    private ObjectMapper mapper = new ObjectMapper();
    private Employee employee;

    @BeforeEach
    public void setUp() {
        employee = Employee.builder()
                .firstName("Legend")
                .lastName("INSPIRE")
                .email("r@sir.com")
                .build();
    }

    @DisplayName("Test get by id (negative)")
    @Test
    void givenEmployeeByIdWhenGetEmployeeIdNegative() throws Exception {
        Long employeeId = 1L;
        given(employeeService.getEmployeeById(employeeId)).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(get("/employees/{id}", employeeId));

        response.andExpect(status().isNotFound())
                .andDo(print());
    }

    @DisplayName("Test for update (negative)")
    @Test
    void givenEmployeeByIdWhenUpdateEmployeeNegative() throws Exception {
        Long employeeId = 1L;
        Employee updatedEmployee = Employee.builder().firstName("LegendR").lastName("LegendS").email("lr@com").build();
        given(employeeService.getEmployeeById(employeeId)).willReturn(Optional.empty());
        given(employeeService.updateEmployee(any(Employee.class))).
                willAnswer((invocation) -> invocation.getArgument(0));

        ResultActions response = mockMvc.perform(put("/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updatedEmployee)));

        response.andExpect(status().isNotFound())
                .andDo(print());
    }

    @DisplayName("Test for update")
    @Test
    void givenEmployeeByIdWhenUpdateEmployee() throws Exception {
        Long employeeId = 1L;
        Employee updatedEmployee = Employee.builder().firstName("LegendR").lastName("LegendS").email("lr@com").build();
        given(employeeService.getEmployeeById(employeeId)).willReturn(Optional.of(employee));
        given(employeeService.updateEmployee(any(Employee.class))).
                willAnswer((invocation) -> invocation.getArgument(0));

        ResultActions response = mockMvc.perform(put("/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updatedEmployee)));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.firstName", is(updatedEmployee.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(updatedEmployee.getLastName())))
                .andExpect(jsonPath("$.email", is(updatedEmployee.getEmail())));
    }

    @DisplayName("Test get by id")
    @Test
    void givenEmployeeByIdWhenGetEmployeeId() throws Exception {
        Long employeeId = 1L;
        given(employeeService.getEmployeeById(employeeId)).willReturn(Optional.of(employee));

        ResultActions response = mockMvc.perform(get("/employees/{id}", employeeId));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.firstName", is(employee.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(employee.getLastName())))
                .andExpect(jsonPath("$.email", is(employee.getEmail())));

    }

    @DisplayName("Test all list")
    @Test
    void getAllEmployeesListTest() throws Exception {
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(Employee.builder().firstName("Lia").lastName("Mia").email("li@com").build());
        employeeList.add(employee);
        given(employeeService.getAllEmployees()).willReturn(employeeList);

        ResultActions response = mockMvc.perform(get("/employees"));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", is(employeeList.size())));
    }

    @DisplayName("Test for create")
    @Test
    void givenEmployeeWhenGetCreateEmployeeThenReturnEmployee() throws Exception {

        given(employeeService.savedEmployee(any(Employee.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));


        ResultActions response = mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(employee)));

        response
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(employee.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(employee.getLastName())))
                .andExpect(jsonPath("$.email", is(employee.getEmail())));

    }

    @DisplayName("Test for delete")
    @Test
    void voidEmployeeWhenGetDelete() throws Exception {
        Long employeeId = 1L;
        willDoNothing().given(employeeService).deleteEmployee(employeeId);

        ResultActions response = mockMvc.perform(delete("/employees/{id}", employeeId));

        response.andExpect(status().isOk())
                .andDo(print());
    }
}