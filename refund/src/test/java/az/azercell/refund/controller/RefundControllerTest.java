package az.azercell.refund.controller;

import az.azercell.refund.exceptions.CustomerNotFoundException;
import az.azercell.refund.exceptions.InvalidRefundAmountException;
import az.azercell.refund.exceptions.TokenExpiredException;
import az.azercell.refund.service.TransactionService;
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

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RefundControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @InjectMocks
    private RefundController refundController;
    @Mock
    private TransactionService transactionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(refundController).build();
    }

    @Test
    public void testMakeRefund() {

        when(transactionService.makeRefund(anyString(), anyDouble())).thenReturn("Refund successful.");

        // Perform the test
        ResponseEntity<String> response = refundController.makeRefund("mockedToken", 50.0);

        // Assert the response
        assertAll(() -> {
            verify(transactionService, times(1)).makeRefund(anyString(), anyDouble());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Refund successful.", response.getBody());
        });
    }

    @Test
    public void testValidRefund() {

        when(transactionService.makeRefund(anyString(), anyDouble())).thenReturn("Refund successful.");

        ResponseEntity<String> response = refundController.makeRefund("mockedToken", 50.0);

        assertAll(() -> {
            verify(transactionService, times(1)).makeRefund(anyString(), anyDouble());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Refund successful.", response.getBody());
        });
    }

    @Test
    public void testExpiredToken() throws Exception {
        double validAmount = 50.0;
        String expiredToken = "expiredToken"; // Expired token

        when(transactionService.makeRefund(anyString(), anyDouble())).thenThrow(TokenExpiredException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/refunds/make-refund")
                        .header("Authorization", expiredToken)
                        .param("amount", String.valueOf(validAmount)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Token has expired."));

        verify(transactionService, times(1)).makeRefund(anyString(), anyDouble());
    }

    @Test
    public void testCustomerNotFound() throws Exception {
        double validAmount = 50.0;
        String invalidToken = "invalidToken"; // Invalid token

        when(transactionService.makeRefund(anyString(), anyDouble())).thenThrow(CustomerNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/refunds/make-refund")
                        .header("Authorization", invalidToken)
                        .param("amount", String.valueOf(validAmount)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Customer not found."));

        verify(transactionService, times(1)).makeRefund(anyString(), anyDouble());
    }

    @Test
    public void testInvalidRefundAmount() throws Exception {
        double invalidAmount = -10.0; // Negative refund amount

        when(transactionService.makeRefund(anyString(), anyDouble())).thenThrow(InvalidRefundAmountException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/refunds/make-refund")
                        .header("Authorization", "validToken")
                        .param("amount", String.valueOf(invalidAmount)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid amount for refund."));

        verify(transactionService, times(1)).makeRefund(anyString(), anyDouble());
    }
}
