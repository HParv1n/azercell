package az.azercell.purchase.service;

import az.azercell.purchase.client.CustomerMicroserviceClient;
import az.azercell.purchase.dto.CustomerDTO;
import az.azercell.purchase.dto.TransactionDTO;
import az.azercell.purchase.exceptions.CustomerNotFoundException;
import az.azercell.purchase.exceptions.InvalidRefundAmountException;
import az.azercell.purchase.exceptions.TokenExpiredException;
import az.azercell.purchase.model.Transaction;
import az.azercell.purchase.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerMicroserviceClient customerMicroserviceClient;

    private CustomerDTO createCustomerDTO() {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("Julian");
        customerDTO.setSurname("Alvarez");
        customerDTO.setGsmNumber("994501234567");
        customerDTO.setBalance(BigDecimal.valueOf(200.0));

        return customerDTO;
    }


    @Test
    public void testValidatePurchaseAmount_ValidAmount() {
        double amount = 50.0;
        double balance = 100.0;

        assertDoesNotThrow(() -> transactionService.validatePurchaseAmount(amount, balance));
    }

    @Test
    public void testValidatePurchaseAmount_InvalidAmount() {
        double amount = 150.0;
        double balance = 100.0;

        assertThrows(InvalidRefundAmountException.class, () -> transactionService.validatePurchaseAmount(amount, balance));
    }

    @Test
    public void testGetCustomerByGsmNumber_CustomerFound() {
        String gsmNumber = "994501234567";
        CustomerDTO customer = createCustomerDTO();

        when(customerMicroserviceClient.getCustomerByGsmNumber(gsmNumber)).thenReturn(customer);

        CustomerDTO result = transactionService.getCustomerByGsmNumber(gsmNumber);

        assertNotNull(result);
        assertEquals(customer.getName(), result.getName());
    }

    @Test
    public void testGetCustomerByGsmNumber_CustomerNotFound() {
        String gsmNumber = "994501234567";

        when(customerMicroserviceClient.getCustomerByGsmNumber(gsmNumber)).thenReturn(null);

        assertThrows(CustomerNotFoundException.class, () -> transactionService.getCustomerByGsmNumber(gsmNumber));
    }

    @Test
    public void testUpdateCustomerBalance() {
        CustomerDTO customer = createCustomerDTO();
        BigDecimal newBalance = BigDecimal.valueOf(150.0);

        doNothing().when(customerMicroserviceClient).updateCustomer(any(CustomerDTO.class), anyLong());

        assertDoesNotThrow(() -> transactionService.updateCustomerBalance(customer, newBalance));
    }

    @Test
    public void testSaveTransaction() {
        CustomerDTO customer = createCustomerDTO();
        double amount = 50.0;
        BigDecimal newBalance = BigDecimal.valueOf(150.0);

        TransactionDTO transactionDTO = transactionService.customerToTransactionDTO(customer, Optional.empty(), amount, newBalance);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        assertDoesNotThrow(() -> transactionService.saveTransaction(customer, amount, newBalance));
    }


    @Test
    public void testMakePurchase_CustomerNotFound() {
        String jwtToken = "validToken";
        double purchaseAmount = 50.0;

        when(customerMicroserviceClient.getCustomerByGsmNumber(anyString())).thenReturn(null);

        assertThrows(CustomerNotFoundException.class, () -> transactionService.makePurchase(jwtToken, purchaseAmount));
    }

    @Test
    public void testUpdateCustomerBalance_SuccessfulUpdate() {
        CustomerDTO customer = createCustomerDTO();
        BigDecimal newBalance = BigDecimal.valueOf(150);

        doNothing().when(customerMicroserviceClient).updateCustomer(any(CustomerDTO.class), anyLong());

        assertDoesNotThrow(() -> transactionService.updateCustomerBalance(customer, newBalance));
    }

    @Test
    public void testUpdateCustomerBalance_FailedUpdate() {
        CustomerDTO customer = createCustomerDTO();
        BigDecimal newBalance = BigDecimal.valueOf(150);

        doThrow(new RuntimeException("Update failed")).when(customerMicroserviceClient).updateCustomer(any(CustomerDTO.class), anyLong());

        assertThrows(RuntimeException.class, () -> transactionService.updateCustomerBalance(customer, newBalance));
    }


}