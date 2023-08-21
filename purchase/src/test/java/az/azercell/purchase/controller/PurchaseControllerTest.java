package az.azercell.purchase.controller;

import az.azercell.purchase.dto.CustomerDTO;
import az.azercell.purchase.exceptions.CustomerNotFoundException;
import az.azercell.purchase.exceptions.InvalidRefundAmountException;
import az.azercell.purchase.exceptions.TokenExpiredException;
import az.azercell.purchase.service.TransactionService;
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
public class PurchaseControllerTest {


    @Autowired
    private MockMvc mockMvc;
    @InjectMocks
    private PurchaseController purchaseController;
    @Mock
    private TransactionService transactionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(purchaseController).build();
    }

    @Test
    public void testMakePurchase() {

        when(transactionService.makePurchase(anyString(), anyDouble())).thenReturn("Purchase successful.");

        // Perform the test
        ResponseEntity<String> response = purchaseController.makePurchase("mockedToken", 50.0);

        // Assert the response
        assertAll(() -> {
            verify(transactionService, times(1)).makePurchase(anyString(), anyDouble());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Purchase successful.", response.getBody());
        });
    }

    @Test
    public void testValidPurchase() {

        when(transactionService.makePurchase(anyString(), anyDouble())).thenReturn("Purchase successful.");

        ResponseEntity<String> response = purchaseController.makePurchase("mockedToken", 50.0);

        assertAll(() -> {
            verify(transactionService, times(1)).makePurchase(anyString(), anyDouble());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Purchase successful.", response.getBody());
        });
    }

    @Test
    public void testExpiredToken() throws Exception {
        double validAmount = 50.0;
        String expiredToken = "expiredToken"; // Expired token

        when(transactionService.makePurchase(anyString(), anyDouble())).thenThrow(TokenExpiredException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/purchases/make-purchase")
                        .header("Authorization", expiredToken)
                        .param("amount", String.valueOf(validAmount)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Token has expired."));

        verify(transactionService, times(1)).makePurchase(anyString(), anyDouble());
    }

    @Test
    public void testCustomerNotFound() throws Exception {
        double validAmount = 50.0;
        String invalidToken = "invalidToken"; // Invalid token

        when(transactionService.makePurchase(anyString(), anyDouble())).thenThrow(CustomerNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/purchases/make-purchase")
                        .header("Authorization", invalidToken)
                        .param("amount", String.valueOf(validAmount)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Customer not found."));

        verify(transactionService, times(1)).makePurchase(anyString(), anyDouble());
    }

    @Test
    public void testInvalidPurchaseAmount() throws Exception {
        double invalidAmount = -10.0; //invalid amount

        when(transactionService.makePurchase(anyString(), anyDouble())).thenThrow(InvalidRefundAmountException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/purchases/make-purchase")
                        .header("Authorization", "validToken")
                        .param("amount", String.valueOf(invalidAmount)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid amount for Purchase."));

        verify(transactionService, times(1)).makePurchase(anyString(), anyDouble());
    }
}