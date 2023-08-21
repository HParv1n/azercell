package az.azercell.refund.service;

import az.azercell.refund.client.CustomerMicroserviceClient;
import az.azercell.refund.dto.CustomerDTO;
import az.azercell.refund.dto.TransactionDTO;
import az.azercell.refund.enumeration.CreatedBy;
import az.azercell.refund.enumeration.TransactionType;
import az.azercell.refund.exceptions.CustomerNotFoundException;
import az.azercell.refund.exceptions.InvalidRefundAmountException;
import az.azercell.refund.generic.GenericServiceImpl;
import az.azercell.refund.model.Transaction;
import az.azercell.refund.repository.TransactionRepository;
import az.azercell.refund.util.JwtTokenUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

import static az.azercell.refund.util.CalculateBalance.calculateNewBalance;
import static az.azercell.refund.util.JwtTokenUtil.validateToken;
import static az.azercell.refund.util.TransactionValidationHelper.validateCustomer;


@Service
public class TransactionService extends GenericServiceImpl<TransactionDTO, Transaction> {
    private final TransactionRepository repository;
    private final CustomerMicroserviceClient customerMicroserviceClient;

    public TransactionService(TransactionRepository repository,
                              CustomerMicroserviceClient customerMicroserviceClient) {
        super(repository, TransactionDTO::new, TransactionService::dtoToEntity);
        this.repository = repository;
        this.customerMicroserviceClient = customerMicroserviceClient;
    }

    public String makeRefund(String jwtToken, double amount) {

        validateToken(jwtToken);

        String gsmNumber = extractPhoneNumberFromToken(jwtToken);
        CustomerDTO customer = getCustomerByGsmNumber(gsmNumber);
        validateCustomer(customer);

        Transaction transaction = getLastPurchaseTransaction(customer.getId());
        validateRefundAmount(amount, transaction);

        BigDecimal newBalance = calculateNewBalance(customer.getBalance(), amount);
        saveTransaction(customer, amount, newBalance, transaction.getId());
        updateCustomerBalance(customer, newBalance);

        return "Refund successful. Refund transaction: " + amount;
    }

    protected CustomerDTO getCustomerByGsmNumber(String gsmNumber) {
        CustomerDTO customer = customerMicroserviceClient.getCustomerByGsmNumber(gsmNumber);
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found.");
        }
        return customer;
    }


    protected void updateCustomerBalance(CustomerDTO customer, BigDecimal newBalance) {
        customer.setBalance(newBalance);
        customerMicroserviceClient.updateCustomer(customer, customer.getId());
    }

    protected String extractPhoneNumberFromToken(String jwtToken) {
        return JwtTokenUtil.extractPhoneNumber(jwtToken);
    }

    protected void validateRefundAmount(double amount, Transaction transaction) {
        double lastPurchase = transaction.getAmount().doubleValue();
        double totalRefund = getTotalRefund(transaction.getId(), transaction.getCustomerId());

        if (amount > 0.01 || amount > (lastPurchase - totalRefund)) {
            throw new InvalidRefundAmountException("Invalid amount for refund.");
        }
    }


    protected double getTotalRefund(Long parentId, Long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .filter(transaction -> transaction.getTransactionTypeId() == TransactionType.REFUND.getOrder() &&
                        (parentId == null || parentId.equals(transaction.getParentId())))
                .mapToDouble(transaction -> transaction.getAmount().doubleValue())
                .sum();
    }

    protected void saveTransaction(CustomerDTO customer, double amount, BigDecimal newBalance, Long transactionId) {
        TransactionDTO transactionDTO = customerToTransactionDTO(customer, Optional.empty(), amount, newBalance, transactionId);
        save(transactionDTO);
    }


    @Override
    public Transaction save(TransactionDTO dto) {
        Transaction entity = dtoToEntity(dto, Optional.empty());
        Transaction saved = repository.save(entity);
        return saved;
    }

    protected Transaction getLastPurchaseTransaction(Long customerId) {
        List<Transaction> transactionList = repository.findByCustomerId(customerId);
        Transaction transaction = transactionList.stream()
                .filter(transaction1 -> TransactionType.OUT.getOrder() == transaction1.getTransactionTypeId())
                .reduce((first, last) -> last)
                .orElse(null);

        assert transaction != null;
        return transaction;
    }

    protected static Transaction dtoToEntity(TransactionDTO dto, Optional<Transaction> existingEntity) {
        Transaction entity = existingEntity.orElseGet(Transaction::new);
        entity.setId(dto.getId());
        entity.setAmount(dto.getAmount());
        entity.setAfterAmount(dto.getAfterAmount());
        entity.setParentId(dto.getParentId());
        entity.setBeforeAmount(dto.getBeforeAmount());
        entity.setOperatedAt(dto.getOperatedAt());
        entity.setTransactionTypeId(dto.getTransactionTypeId());
        entity.setCustomerId(dto.getCustomerId());
        entity.setCreatedById(dto.getCreatedById());

        return entity;
    }

    private static TransactionDTO customerToTransactionDTO(CustomerDTO customer,
                                                           Optional<TransactionDTO> existingDto,
                                                           double amount, BigDecimal newBalance, Long id) {
        TransactionDTO dto = existingDto.orElseGet(TransactionDTO::new);
        dto.setAmount(BigDecimal.valueOf(amount));
        dto.setAfterAmount(customer.getBalance());
        dto.setParentId(id);
        dto.setBeforeAmount(newBalance);
        dto.setOperatedAt(LocalDateTime.now());
        dto.setTransactionTypeId(TransactionType.REFUND.getOrder());
        dto.setCustomerId(customer.getId());
        dto.setCreatedById(CreatedBy.CUSTOMER.getOrder());

        return dto;
    }

}
