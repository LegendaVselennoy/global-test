package org.example.employee.repository;

import org.example.entity.Employee;
import org.example.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        employee = Employee.builder()
                .firstName("Legend")
                .lastName("INSPIRE")
                .email("r@sir.com")
                .build();

        employeeRepository.save(employee);
    }

    @DisplayName("JUnit test list")
    @Test
    void findAllEmployeeList() {
        Employee employee1 = Employee.builder()
                .firstName("Legenda")
                .lastName("INSPIREA")
                .email("l@sir.com")
                .build();

        employeeRepository.save(employee1);

        List<Employee> employeeList = employeeRepository.findAll();

        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(2);
    }

    @DisplayName("JUnit test fot email")
    @Test
    public void givenEmail() {

        Employee employeeDB = employeeRepository.findByEmail(employee.getEmail()).get();

        assertThat(employeeDB).isNotNull();
    }

    @DisplayName("JUnit test for update")
    @Test
    public void updateEmployee() {

        Employee savedEmployee = employeeRepository.findById(employee.getId()).get();
        savedEmployee.setFirstName("r@sir.com");
        savedEmployee.setFirstName("LegendR");
        Employee updatedEmployee = employeeRepository.save(savedEmployee);

        assertThat(updatedEmployee.getEmail()).isEqualTo("r@sir.com");
        assertThat(updatedEmployee.getFirstName()).isEqualTo("LegendR");
    }

    @DisplayName("JUnit test for save")
    @Test
    void employeeSaveThenReturnSavedEmployee() {
        Employee savedEmployee = employeeRepository.save(employee);

        assertThat(savedEmployee).isNotNull();
        assertThat(savedEmployee.getId()).isGreaterThan(0);
    }

    @DisplayName("Test JPQL Params")
    @Test
    public void givenFirstNameAndLastNameWhenFindByJPQLNamedParamsThenReturnEmployee() {
        String firstName = "Legend";
        String lastName = "INSPIRE";

        Employee savedEmployee = employeeRepository.findByJPQLNamedParams(firstName, lastName);

        assertThat(savedEmployee).isNotNull();
    }

    @DisplayName("Test for Native SQL")
    @Test
    public void givenFirstNameAndLastNameWhenFindByNativeSQLThenReturnEmployee() {
        Employee savedEmployee = employeeRepository
                .findByNativeSQL(employee.getFirstName(), employee.getLastName());

        assertThat(savedEmployee).isNotNull();
    }

    @DisplayName("Test for Native Params SQL")
    @Test
    public void givenFirstNameAndLastNameWhenFindByNativeSQLNamedParamsThenReturnEmployee() {
        Employee savedEmployee = employeeRepository
                .findByNativeSQLNamed(employee.getFirstName(), employee.getLastName());

        assertThat(savedEmployee).isNotNull();
    }

    @DisplayName("Test for JPQL")
    @Test
    public void givenFirstNameAndLastNameWhenFindByJPQLThenReturnEmployee() {
        String firstName = "Legend";
        String lastName = "INSPIRE";

        Employee savedEmployee = employeeRepository.findByJPQL(firstName, lastName);

        assertThat(savedEmployee).isNotNull();
    }

    @DisplayName("JUnit test for findById")
    @Test
    public void employeeFindByIdThenReturnEmployee() {
        Employee employeeDB = employeeRepository.findById(employee.getId()).orElse(null);

        assertThat(employeeDB).isNotNull();
    }

    @DisplayName("JUnit test for delete")
    @Test
    public void employeeDeleteThenReturnEmployee() {
        employeeRepository.delete(employee);
        Optional<Employee> employeeDB = employeeRepository.findById(employee.getId());

        assertThat(employeeDB).isEmpty();
    }
}