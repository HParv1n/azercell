package az.azercell.refund.service;

import az.azercell.refund.client.CustomerMicroserviceClient;
import az.azercell.refund.dto.CustomerDTO;
import az.azercell.refund.dto.TransactionDTO;
import az.azercell.refund.enumeration.CreatedBy;
import az.azercell.refund.enumeration.TransactionType;
import az.azercell.refund.exceptions.CustomerNotFoundException;
import az.azercell.refund.exceptions.InvalidRefundAmountException;
import az.azercell.refund.exceptions.TokenExpiredException;
import az.azercell.refund.model.Transaction;
import az.azercell.refund.repository.TransactionRepository;
import az.azercell.refund.util.JwtTokenUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static az.azercell.refund.service.TransactionService.dtoToEntity;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private CustomerMicroserviceClient customerMicroserviceClient;


    @Test
    public void testMakeRefund_CustomerNotFound() {
        String jwtToken = "validToken";
        double refundAmount = 50.0;

        // Mock service to throw CustomerNotFoundException
        when(customerMicroserviceClient.getCustomerByGsmNumber(anyString())).thenThrow(CustomerNotFoundException.class);

        // Perform the refund
        assertThrows(CustomerNotFoundException.class, () -> transactionService.makeRefund(jwtToken, refundAmount));
    }


    @Test
    public void testGetCustomerByGsmNumber_CustomerFound() {
        // Mock Customer data
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setGsmNumber("123456789");

        // create mock customerMicroserviceClient
        CustomerMicroserviceClient customerMicroserviceClient = mock(CustomerMicroserviceClient.class);

        // create mock for customerMicroserviceClient.getCustomerByGsmNumber
        when(customerMicroserviceClient.getCustomerByGsmNumber("123456789")).thenReturn(customer);

        // create TransactionService and inject mock customerMicroserviceClient
        TransactionService transactionService = new TransactionService(null, customerMicroserviceClient);

        // call method for test
        CustomerDTO result = transactionService.getCustomerByGsmNumber("123456789");


        Assertions.assertNotNull(result);
        Assertions.assertEquals("John", result.getName());
        Assertions.assertEquals("Doe", result.getSurname());

        verify(customerMicroserviceClient, times(1)).getCustomerByGsmNumber("123456789");
    }

    @Test
    public void testGetCustomerByGsmNumber_CustomerNotFound() {

        CustomerMicroserviceClient customerMicroserviceClient = mock(CustomerMicroserviceClient.class);

        when(customerMicroserviceClient.getCustomerByGsmNumber("987654321")).thenReturn(null);

        TransactionService transactionService = new TransactionService(null, customerMicroserviceClient);

        assertThrows(CustomerNotFoundException.class, () -> transactionService.getCustomerByGsmNumber("987654321"));

        verify(customerMicroserviceClient, times(1)).getCustomerByGsmNumber("987654321");
    }

    @Test
    public void testUpdateCustomerBalance_SuccessfulUpdate() {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);
        customer.setName("Julian");
        customer.setSurname("Alvarez");
        customer.setBalance(BigDecimal.valueOf(100));

        CustomerMicroserviceClient customerMicroserviceClient = mock(CustomerMicroserviceClient.class);

        doNothing().when(customerMicroserviceClient).updateCustomer(any(CustomerDTO.class), anyLong());

        TransactionService transactionService = new TransactionService(null, customerMicroserviceClient);

        BigDecimal newBalance = BigDecimal.valueOf(150);
        transactionService.updateCustomerBalance(customer, newBalance);

        verify(customerMicroserviceClient, times(1)).updateCustomer(customer, customer.getId());

        Assertions.assertEquals(newBalance, customer.getBalance());
    }

    @Test
    public void testValidateRefundAmount_HighRefundAmount() {
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(100));

        double highRefundAmount = 200.0;

        InvalidRefundAmountException exception = assertThrows(InvalidRefundAmountException.class,
                () -> transactionService.validateRefundAmount(highRefundAmount, transaction));

        Assertions.assertEquals("Invalid amount for refund.", exception.getMessage());
    }


    @Test
    public void testGetTotalRefund_SpecificParentId() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(createTransaction(1L, 100.0));
        transactions.add(createTransaction(2L, 50.0));
        transactions.add(createTransaction(1L, 75.0));

        when(transactionRepository.findByCustomerId(anyLong())).thenReturn(transactions);

        double totalRefund = transactionService.getTotalRefund(1L, 1L);

        Assertions.assertEquals(175.0, totalRefund, 0.01);
    }

    @Test
    public void testGetTotalRefund_NoParentId() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(createTransaction(null, 100.0));
        transactions.add(createTransaction(null, 50.0));
        transactions.add(createTransaction(null, 75.0));

        when(transactionRepository.findByCustomerId(anyLong())).thenReturn(transactions);

        double totalRefund = transactionService.getTotalRefund(null, 1L);

        Assertions.assertEquals(225.0, totalRefund, 0.01);
    }

    @Test
    public void testGetTotalRefund_NoData() {
        List<Transaction> transactions = new ArrayList<>();

        when(transactionRepository.findByCustomerId(anyLong())).thenReturn(transactions);

        double totalRefund = transactionService.getTotalRefund(null, 1L);

        Assertions.assertEquals(0.0, totalRefund, 0.01);
    }

    protected Transaction createTransaction(Long parentId, double amount) {
        Transaction transaction = new Transaction();
        transaction.setParentId(parentId);
        transaction.setAmount(BigDecimal.valueOf(amount));
        transaction.setTransactionTypeId(TransactionType.REFUND.getOrder());
        return transaction;
    }


    @Test
    public void testSaveTransaction_SuccessfulSave() {
        CustomerDTO customer = createCustomer(1L, "Alice", "Johnson", BigDecimal.valueOf(100));
        double amount = 50.0;
        BigDecimal newBalance = BigDecimal.valueOf(50);
        Long transactionId = 1L;

        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setCustomerId(customer.getId());
        transactionDTO.setAmount(BigDecimal.valueOf(amount));
        transactionDTO.setBeforeAmount(newBalance);
        transactionDTO.setParentId(transactionId);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        transactionService.saveTransaction(customer, amount, newBalance, transactionId);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testSaveTransaction_InvalidCustomer() {
        CustomerDTO invalidCustomer = null;
        double amount = 50.0;
        BigDecimal newBalance = BigDecimal.valueOf(50);
        Long transactionId = 1L;

        assertThrows(NullPointerException.class, () -> transactionService.saveTransaction(invalidCustomer, amount, newBalance, transactionId));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }


    private CustomerDTO createCustomer(Long id, String name, String surname, BigDecimal balance) {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(id);
        customer.setName(name);
        customer.setSurname(surname);
        customer.setBalance(balance);
        return customer;
    }

    private TransactionDTO createTransactionDTO() {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(1L);
        dto.setAmount(BigDecimal.valueOf(50));
        dto.setAfterAmount(BigDecimal.valueOf(150));
        dto.setParentId(2L);
        dto.setBeforeAmount(BigDecimal.valueOf(100));
        dto.setOperatedAt(LocalDateTime.now());
        dto.setTransactionTypeId(TransactionType.REFUND.getOrder());
        dto.setCustomerId(3L);
        dto.setCreatedById(CreatedBy.CUSTOMER.getOrder());
        return dto;
    }

    private void assertTransactionDTOAndEntityEqual(TransactionDTO expectedDto, Transaction actualEntity) {
        Assertions.assertEquals(expectedDto.getId(), actualEntity.getId());
        Assertions.assertEquals(expectedDto.getAmount(), actualEntity.getAmount());
        Assertions.assertEquals(expectedDto.getAfterAmount(), actualEntity.getAfterAmount());
        Assertions.assertEquals(expectedDto.getParentId(), actualEntity.getParentId());
        Assertions.assertEquals(expectedDto.getBeforeAmount(), actualEntity.getBeforeAmount());
        Assertions.assertEquals(expectedDto.getOperatedAt(), actualEntity.getOperatedAt());
        Assertions.assertEquals(expectedDto.getTransactionTypeId(), actualEntity.getTransactionTypeId());
        Assertions.assertEquals(expectedDto.getCustomerId(), actualEntity.getCustomerId());
        Assertions.assertEquals(expectedDto.getCreatedById(), actualEntity.getCreatedById());
    }


    private Transaction createTransactionEntity() {
        Transaction entity = new Transaction();
        entity.setId(1L);
        entity.setAmount(BigDecimal.valueOf(50));
        entity.setAfterAmount(BigDecimal.valueOf(150));
        entity.setParentId(2L);
        entity.setBeforeAmount(BigDecimal.valueOf(100));
        entity.setOperatedAt(LocalDateTime.now());
        entity.setTransactionTypeId(1);
        entity.setCustomerId(3L);
        entity.setCreatedById(4);
        return entity;
    }

    @Test
    public void testSave_Successful() {
        TransactionDTO transactionDTO = createTransactionDTO();

        Transaction savedTransaction = new Transaction();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        Transaction result = transactionService.save(transactionDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedTransaction, result);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testSave_Failed() {
        TransactionDTO transactionDTO = createTransactionDTO();

        when(transactionRepository.save(any(Transaction.class))).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> transactionService.save(transactionDTO));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }


    @Test
    public void testDtoToEntity_Successful() {
        TransactionDTO dto = createTransactionDTO();
        Transaction entity = dtoToEntity(dto, Optional.empty());

        assertTransactionDTOAndEntityEqual(dto, entity);
    }

    @Test
    public void testDtoToEntity_UpdateExistingEntity() {
        TransactionDTO dto = createTransactionDTO();
        Transaction existingEntity = createTransactionEntity();
        Optional<Transaction> existingEntityOptional = Optional.of(existingEntity);

        Transaction entity = dtoToEntity(dto, existingEntityOptional);

        assertTransactionDTOAndEntityEqual(dto, entity);
    }

    @Test
    public void testDtoToEntity_CreateNewEntity() {
        TransactionDTO dto = createTransactionDTO();

        Transaction entity = dtoToEntity(dto, Optional.empty());

        assertTransactionDTOAndEntityEqual(dto, entity);
    }


}

