package az.azercell.customer.controller;

import az.azercell.customer.model.Customer;
import az.azercell.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @InjectMocks
    private CustomerController customerController;

    @Mock
    private CustomerService customerService;

    @Test
    public void testGetCustomerByGsmNumber_Success() {
        String gsmNumber = "1234567890";
        Customer expectedCustomer = new Customer();
        expectedCustomer.setId(1L);
        expectedCustomer.setName("Julian");
        expectedCustomer.setSurname("Alvarez");
        expectedCustomer.setGsmNumber(gsmNumber);

        when(customerService.getCustomerByGsmNumber(gsmNumber)).thenReturn(expectedCustomer);

        ResponseEntity<Customer> response = customerController.getCustomerByGsmNumber(gsmNumber);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCustomer, response.getBody());
    }
}
