package org.example.employee.service;

import org.example.entity.Employee;
import org.example.exception.NotFoundEmployeeException;
import org.example.repository.EmployeeRepository;
import org.example.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTestMock {

    @Mock
    private EmployeeRepository repository;
    @InjectMocks
    private EmployeeServiceImpl service;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        employee = Employee.builder()
                .id(1L)
                .firstName("Legend")
                .lastName("INSPIRE")
                .email("r@sir.com")
                .build();
    }

    @DisplayName("Test for update")
    @Test
    void givenEmployeeWhenGetUpdateThenReturnEmployee() {
        given(repository.save(employee)).willReturn(employee);
        employee.setFirstName("New name");
        employee.setLastName("New last name");
        employee.setEmail("R@sir.com");

        Employee updateEmployee = service.updateEmployee(employee);

        assertThat(updateEmployee.getFirstName()).isEqualTo(employee.getFirstName());
        assertThat(updateEmployee.getLastName()).isEqualTo(employee.getLastName());
        assertThat(updateEmployee.getEmail()).isEqualTo(employee.getEmail());
    }

    @DisplayName("Test for delete")
    @Test
    void givenEmployeeWhenGetDeleteThenReturnEmployee() {
        Long employeeId = 1L;
        willDoNothing().given(repository).deleteById(employeeId);

        service.deleteEmployee(employeeId);

        verify(repository, times(1)).deleteById(employeeId);
    }

    @DisplayName("Test for findById")
    @Test
    void givenEmployeeWhenGetEmployeeByIdThenReturnEmployee() {
        given(repository.findById(1L)).willReturn(Optional.of(employee));

        Employee savedEmployee = service.getEmployeeById(employee.getId()).get();

        assertThat(savedEmployee).isNotNull();
    }

    @DisplayName("Test for findAll (negative)")
    @Test
    void getAllEmployeesListTestNegative() {
        given(repository.findAll()).willReturn(Collections.emptyList());

        List<Employee> employeeList = service.getAllEmployees();

        assertThat(employeeList).isEmpty();
        assertThat(employeeList.size()).isEqualTo(0);
    }

    @DisplayName("Test for findAll")
    @Test
    void getAllEmployeesListTest() {
        Employee employee1 = Employee.builder()
                .id(2L)
                .firstName("Legend")
                .lastName("INSPIRE")
                .email("r@sir.com")
                .build();
        given(repository.findAll()).willReturn(List.of(employee, employee1));

        List<Employee> employeeList = service.getAllEmployees();

        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(2);
    }

    @DisplayName("Test for save with exception")
    @Test
    void givenEmployeeByEmailThrowsException() {
        // given
        given(repository.findByEmail(employee.getEmail()))
                .willReturn(Optional.of(employee));

        // when
        assertThrows(NotFoundEmployeeException.class, () -> {
            service.savedEmployee(employee);
        });

        // then
        verify(repository, never()).save(any(Employee.class));
    }
}
