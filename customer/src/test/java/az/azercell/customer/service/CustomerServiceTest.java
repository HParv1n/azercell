package az.azercell.customer.service;

import az.azercell.customer.dto.CustomerDTO;
import az.azercell.customer.exceptions.CustomerNotFoundException;
import az.azercell.customer.model.Customer;
import az.azercell.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CustomerServiceTest {

    @MockBean
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    @Test
    void testGetCustomerByGsmNumber_ValidNumber() {
        // Arrange
        String gsmNumber = "1234567890";
        Customer customer = new Customer();
        customer.setGsmNumber(gsmNumber);
        when(customerRepository.findByGsmNumber(gsmNumber)).thenReturn(Collections.singletonList(customer));

        // Act
        Customer result = customerService.getCustomerByGsmNumber(gsmNumber);

        // Assert
        assertNotNull(result);
        assertEquals(gsmNumber, result.getGsmNumber());
    }

    @Test
    void testGetCustomerByGsmNumber_Exception() {
        // Arrange
        String gsmNumber = "1234567890";
        when(customerRepository.findByGsmNumber(gsmNumber)).thenThrow(new RuntimeException("Database error"));

        // Assert & Act
        assertThrows(RuntimeException.class, () -> customerService.getCustomerByGsmNumber(gsmNumber));
    }

    @Test
    void testSaveCustomer() {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("Julian");
        customerDTO.setSurname("Alvarez");
        customerDTO.setBirthdate("27-02-1998");
        customerDTO.setBalance(BigDecimal.ZERO);
        customerDTO.setGsmNumber("1234567890");

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName("Julian");
        savedCustomer.setSurname("Alvarez");
        savedCustomer.setBirthdate("27-02-1998");
        savedCustomer.setBalance(BigDecimal.ZERO);
        savedCustomer.setGsmNumber("1234567890");
        when(customerRepository.save(any())).thenReturn(savedCustomer);

        // Act
        Customer result = customerService.save(customerDTO);

        // Assert
        assertNotNull(result);
        assertEquals(savedCustomer.getId(), result.getId());
        assertEquals(customerDTO.getName(), result.getName());
        assertEquals(customerDTO.getSurname(), result.getSurname());
        assertEquals(customerDTO.getBirthdate(), result.getBirthdate());
        assertEquals(customerDTO.getBalance(), result.getBalance());
        assertEquals(customerDTO.getGsmNumber(), result.getGsmNumber());
    }

    @Test
    void testUpdateCustomer_ValidId() {
        // Arrange
        Long customerId = 1L;
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("Updated Name");

        Customer existingCustomer = new Customer();
        existingCustomer.setId(customerId);
        existingCustomer.setName("Old Name");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));

        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(customerId);
        updatedCustomer.setName("Updated Name");
        when(customerRepository.save(any())).thenReturn(updatedCustomer);

        // Act
        Optional<CustomerDTO> result = customerService.update(customerId, customerDTO);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(customerId, result.get().getId());
        assertEquals(customerDTO.getName(), result.get().getName());
    }

    @Test
    void testUpdateCustomer_InvalidId() {
        // Arrange
        Long customerId = 2L;
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("Updated Name");

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act
        Optional<CustomerDTO> result = customerService.update(customerId, customerDTO);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteCustomer_ValidId() {
        // Arrange
        Long customerId = 1L;
        Customer existingCustomer = new Customer();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));

        // Act
        customerService.delete(customerId);

        // Assert
        verify(customerRepository, times(1)).deleteById(customerId);
    }

    @Test
    void testDeleteCustomer_InvalidId() {
        // Arrange
        Long customerId = 2L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertDoesNotThrow(() -> customerService.delete(customerId));
    }
}