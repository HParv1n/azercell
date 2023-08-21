package az.azercell.topup.service;

import az.azercell.topup.client.CustomerMicroserviceClient;
import az.azercell.topup.dto.CustomerDTO;
import az.azercell.topup.dto.TransactionDTO;
import az.azercell.topup.enumeration.CreatedBy;
import az.azercell.topup.enumeration.TransactionType;
import az.azercell.topup.exceptions.CustomerNotFoundException;
import az.azercell.topup.exceptions.InvalidRefundAmountException;
import az.azercell.topup.model.Transaction;
import az.azercell.topup.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TransactionServiceTests {


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
    public void testAddFunds_InvalidTopUpAmount() {
        String jwtToken = "validToken";
        double topUpAmount = 1200.0;

        // Mock customer data
        CustomerDTO customer = createCustomerDTO();

        // Mock service calls
        when(customerMicroserviceClient.getCustomerByGsmNumber(anyString())).thenReturn(customer);

        assertThrows(InvalidRefundAmountException.class, () -> transactionService.addFunds(jwtToken, topUpAmount));
        verify(customerMicroserviceClient, never()).updateCustomer(any(CustomerDTO.class), anyLong());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testAddFunds_CustomerNotFound() {
        String jwtToken = "validToken";
        double topUpAmount = 500.0;

        // Mock service calls
        when(customerMicroserviceClient.getCustomerByGsmNumber(anyString())).thenReturn(null);

        assertThrows(CustomerNotFoundException.class, () -> transactionService.addFunds(jwtToken, topUpAmount));
        verify(customerMicroserviceClient, never()).updateCustomer(any(CustomerDTO.class), anyLong());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testValidateTopUpAmount_ValidAmount() {
        double validAmount = 500.0;
        assertDoesNotThrow(() -> transactionService.validateTopUpAmount(validAmount));
    }

    @Test
    public void testValidateTopUpAmount_InvalidAmount() {
        double invalidAmount = 1200.0;
        assertThrows(InvalidRefundAmountException.class, () -> transactionService.validateTopUpAmount(invalidAmount));
    }

    @Test
    public void testValidateTopUpAmount_ZeroAmount() {
        double zeroAmount = 0.0;
        assertThrows(InvalidRefundAmountException.class, () -> transactionService.validateTopUpAmount(zeroAmount));
    }

    @Test
    public void testGetCustomerByGsmNumber_CustomerFound() {
        String gsmNumber = "123456789";
        CustomerDTO customer = createCustomerDTO();

        // Mock service call
        when(customerMicroserviceClient.getCustomerByGsmNumber(gsmNumber)).thenReturn(customer);

        CustomerDTO result = transactionService.getCustomerByGsmNumber(gsmNumber);

        assertEquals(customer, result);
    }

    @Test
    public void testGetCustomerByGsmNumber_CustomerNotFound() {
        String gsmNumber = "123456789";

        // Mock service call
        when(customerMicroserviceClient.getCustomerByGsmNumber(gsmNumber)).thenReturn(null);

        assertThrows(CustomerNotFoundException.class, () -> transactionService.getCustomerByGsmNumber(gsmNumber));
    }

    @Test
    public void testDtoToEntity_ExistingEntity() {
        TransactionDTO dto = new TransactionDTO();
        Transaction existingEntity = new Transaction();
        existingEntity.setId(1L);

        Transaction result = TransactionService.dtoToEntity(dto, Optional.of(existingEntity));

        assertEquals(existingEntity.getId(), result.getId());
        assertEquals(dto.getAmount(), result.getAmount());
        assertEquals(dto.getAfterAmount(), result.getAfterAmount());
        assertEquals(dto.getBeforeAmount(), result.getBeforeAmount());
        assertEquals(dto.getOperatedAt(), result.getOperatedAt());
        assertEquals(dto.getTransactionTypeId(), result.getTransactionTypeId());
        assertEquals(dto.getCustomerId(), result.getCustomerId());
        assertEquals(dto.getCreatedById(), result.getCreatedById());
    }

    @Test
    public void testCustomerToTransactionDTO_ExistingDTO() {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);
        customer.setBalance(BigDecimal.valueOf(500));
        double amount = 100.0;
        BigDecimal newBalance = customer.getBalance().subtract(BigDecimal.valueOf(amount));

        TransactionDTO existingDto = new TransactionDTO();
        existingDto.setAmount(BigDecimal.valueOf(50));
        existingDto.setAfterAmount(BigDecimal.valueOf(450));

        TransactionDTO result = TransactionService.customerToTransactionDTO(customer, Optional.of(existingDto), amount, newBalance);

        assertEquals(BigDecimal.valueOf(amount), result.getAmount());
        assertEquals(customer.getBalance(), result.getAfterAmount());
        assertEquals(newBalance, result.getBeforeAmount());
        assertEquals(LocalDateTime.now().getYear(), result.getOperatedAt().getYear());
        assertEquals(TransactionType.IN.getOrder(), result.getTransactionTypeId());
        assertEquals(customer.getId(), result.getCustomerId());
        assertEquals(CreatedBy.CUSTOMER.getOrder(), result.getCreatedById());
    }

    @Test
    public void testCustomerToTransactionDTO_NewDTO() {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);
        customer.setBalance(BigDecimal.valueOf(500));
        double amount = 100.0;
        BigDecimal newBalance = customer.getBalance().subtract(BigDecimal.valueOf(amount));

        TransactionDTO result = TransactionService.customerToTransactionDTO(customer, Optional.empty(), amount, newBalance);

        assertEquals(BigDecimal.valueOf(amount), result.getAmount());
        assertEquals(customer.getBalance(), result.getAfterAmount());
        assertEquals(newBalance, result.getBeforeAmount());
        assertEquals(LocalDateTime.now().getYear(), result.getOperatedAt().getYear());
        assertEquals(TransactionType.IN.getOrder(), result.getTransactionTypeId());
        assertEquals(customer.getId(), result.getCustomerId());
        assertEquals(CreatedBy.CUSTOMER.getOrder(), result.getCreatedById());
    }


}
