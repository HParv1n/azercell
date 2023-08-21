package az.azercell.topup.client;

import az.azercell.topup.controller.TopUpController;
import az.azercell.topup.dto.CustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerMicroserviceClientTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CustomerMicroserviceClient customerMicroserviceClient;

    @InjectMocks
    private TopUpController topUpController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(topUpController).build();
    }

    @Test
    public void testGetCustomerByGsmNumber() {
        String gsmNumber = "1234567890";
        CustomerDTO mockedCustomer = new CustomerDTO();
        mockedCustomer.setId(1L);
        mockedCustomer.setName("Julian");
        mockedCustomer.setSurname("Alvarez");
        mockedCustomer.setGsmNumber(gsmNumber);
        mockedCustomer.setBalance(BigDecimal.valueOf(1000));
        mockedCustomer.setBirthdate("27-02-1999");
        mockedCustomer.setCreatedAt(LocalDateTime.now());

        when(customerMicroserviceClient.getCustomerByGsmNumber(eq(gsmNumber))).thenReturn(mockedCustomer);

        CustomerDTO retrievedCustomer = customerMicroserviceClient.getCustomerByGsmNumber(gsmNumber);

        assertNotNull(retrievedCustomer);
        assertEquals(Optional.of(1L), Optional.ofNullable(retrievedCustomer.getId()));
        assertEquals(mockedCustomer.getName(), retrievedCustomer.getName());
        assertEquals(gsmNumber,retrievedCustomer.getGsmNumber());
        assertEquals(mockedCustomer.getBirthdate(),retrievedCustomer.getBirthdate());
        assertEquals(mockedCustomer.getBalance(),retrievedCustomer.getBalance());
        assertEquals(mockedCustomer.getCreatedAt(),retrievedCustomer.getCreatedAt());

        verify(customerMicroserviceClient, times(1)).getCustomerByGsmNumber(eq(gsmNumber));
    }

    @Test
    public void testUpdateCustomer() {
        CustomerDTO customerToUpdate = new CustomerDTO();
        customerToUpdate.setId(1L);
        customerToUpdate.setName("Updated Name");
        customerToUpdate.setSurname("Updated Surname");
        customerToUpdate.setGsmNumber("1234567890");
        customerToUpdate.setBalance(BigDecimal.valueOf(1000));
        customerToUpdate.setBirthdate("27-02-1999");
        customerToUpdate.setCreatedAt(LocalDateTime.now());

        doNothing().when(customerMicroserviceClient).updateCustomer(eq(customerToUpdate), eq(1L));

        customerMicroserviceClient.updateCustomer(customerToUpdate, 1L);

        verify(customerMicroserviceClient, times(1)).updateCustomer(eq(customerToUpdate), eq(1L));
    }
}
