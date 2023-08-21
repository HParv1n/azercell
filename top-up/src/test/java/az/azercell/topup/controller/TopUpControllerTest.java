package az.azercell.topup.controller;

import az.azercell.topup.exceptions.CustomerNotFoundException;
import az.azercell.topup.exceptions.InvalidRefundAmountException;
import az.azercell.topup.exceptions.TokenExpiredException;
import az.azercell.topup.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TopUpControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @InjectMocks
    private TopUpController topUpController;
    @Mock
    private TransactionService transactionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(topUpController).build();
    }

    @Test
    public void testMakeTopUp() {

        when(transactionService.addFunds(anyString(), anyDouble())).thenReturn("Funds successful.");

        // Perform the test
        ResponseEntity<String> response = topUpController.addFunds("mockedToken", 50.0);

        // Assert the response
        assertAll(() -> {
            verify(transactionService, times(1)).addFunds(anyString(), anyDouble());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Funds successful.", response.getBody());
        });
    }

    @Test
    public void testValidTopUp() {

        when(transactionService.addFunds(anyString(), anyDouble())).thenReturn("Funds successful.");

        ResponseEntity<String> response = topUpController.addFunds("mockedToken", 50.0);

        assertAll(() -> {
            verify(transactionService, times(1)).addFunds(anyString(), anyDouble());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Funds successful.", response.getBody());
        });
    }

    @Test
    public void testExpiredToken() throws Exception {
        double validAmount = 50.0;
        String expiredToken = "expiredToken"; // Expired token

        when(transactionService.addFunds(anyString(), anyDouble())).thenThrow(TokenExpiredException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/top-ups/add-funds")
                        .header("Authorization", expiredToken)
                        .param("amount", String.valueOf(validAmount)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Token has expired."));

        verify(transactionService, times(1)).addFunds(anyString(), anyDouble());
    }

    @Test
    public void testCustomerNotFound() throws Exception {
        double validAmount = 50.0;
        String invalidToken = "invalidToken"; // Invalid token

        when(transactionService.addFunds(anyString(), anyDouble())).thenThrow(CustomerNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/top-ups/add-funds")
                        .header("Authorization", invalidToken)
                        .param("amount", String.valueOf(validAmount)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Customer not found."));

        verify(transactionService, times(1)).addFunds(anyString(), anyDouble());
    }

    @Test
    public void testInvalidTopUpAmount() throws Exception {
        double invalidAmount = -10.0; //invalid amount

        when(transactionService.addFunds(anyString(), anyDouble())).thenThrow(InvalidRefundAmountException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/top-ups/add-funds")
                        .header("Authorization", "validToken")
                        .param("amount", String.valueOf(invalidAmount)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid amount for TopUp."));

        verify(transactionService, times(1)).addFunds(anyString(), anyDouble());
    }
}
