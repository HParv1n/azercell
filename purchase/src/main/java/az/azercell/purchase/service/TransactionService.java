package az.azercell.purchase.service;

import az.azercell.purchase.client.CustomerMicroserviceClient;
import az.azercell.purchase.dto.CustomerDTO;
import az.azercell.purchase.dto.TransactionDTO;
import az.azercell.purchase.enumeration.CreatedBy;
import az.azercell.purchase.enumeration.TransactionType;
import az.azercell.purchase.exceptions.CustomerNotFoundException;
import az.azercell.purchase.exceptions.InvalidRefundAmountException;
import az.azercell.purchase.generic.GenericServiceImpl;
import az.azercell.purchase.model.Transaction;
import az.azercell.purchase.repository.TransactionRepository;
import az.azercell.purchase.util.JwtTokenUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static az.azercell.purchase.util.CalculateBalance.calculateNewBalance;
import static az.azercell.purchase.util.JwtTokenUtil.validateToken;
import static az.azercell.purchase.util.TransactionValidationHelper.validateCustomer;


@Service
public class TransactionService extends GenericServiceImpl<TransactionDTO, Transaction> {
    private final TransactionRepository repository;
    private final CustomerMicroserviceClient customerMicroserviceClient;

    public TransactionService(TransactionRepository repository, CustomerMicroserviceClient customerMicroserviceClient) {
        super(repository, TransactionDTO::new, TransactionService::dtoToEntity);
        this.repository = repository;
        this.customerMicroserviceClient = customerMicroserviceClient;
    }

    public String makePurchase(String jwtToken, double amount) {

        validateToken(jwtToken);

        String gsmNumber = extractPhoneNumberFromToken(jwtToken);
        CustomerDTO customer = getCustomerByGsmNumber(gsmNumber);
        validateCustomer(customer);

        validatePurchaseAmount(amount, customer.getBalance().doubleValue());

        BigDecimal newBalance = calculateNewBalance(customer.getBalance(), amount);
        saveTransaction(customer, amount, newBalance);
        updateCustomerBalance(customer, newBalance);

        return "Purchase successful. New balance: " + newBalance;
    }

    protected void validatePurchaseAmount(double amount, double balance) {

        if (amount < 0.01 || amount > balance) {
            throw new InvalidRefundAmountException("Invalid amount for Purchase.");
        }
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

    private String extractPhoneNumberFromToken(String jwtToken) {
        return JwtTokenUtil.extractPhoneNumber(jwtToken);
    }

    protected void saveTransaction(CustomerDTO customer, double amount, BigDecimal newBalance) {
        TransactionDTO transactionDTO = customerToTransactionDTO(customer, Optional.empty(), amount, newBalance);
        save(transactionDTO);
    }

    @Override
    public Transaction save(TransactionDTO dto) {
        Transaction entity = dtoToEntity(dto, Optional.empty());
        Transaction saved = repository.save(entity);
        return saved;
    }


    private static Transaction dtoToEntity(TransactionDTO dto, Optional<Transaction> existingEntity) {
        Transaction entity = existingEntity.orElseGet(Transaction::new);
        entity.setId(dto.getId());
        entity.setAmount(dto.getAmount());
        entity.setAfterAmount(dto.getAfterAmount());
        entity.setBeforeAmount(dto.getBeforeAmount());
        entity.setOperatedAt(dto.getOperatedAt());
        entity.setTransactionTypeId(dto.getTransactionTypeId());
        entity.setCustomerId(dto.getCustomerId());
        entity.setCreatedById(dto.getCreatedById());

        return entity;
    }

    protected static TransactionDTO customerToTransactionDTO(CustomerDTO customer,
                                                             Optional<TransactionDTO> existingDto,
                                                             double amount, BigDecimal newBalance) {
        TransactionDTO dto = existingDto.orElseGet(TransactionDTO::new);
        dto.setAmount(BigDecimal.valueOf(amount));
        dto.setAfterAmount(customer.getBalance());
        dto.setBeforeAmount(newBalance);
        dto.setOperatedAt(LocalDateTime.now());
        dto.setTransactionTypeId(TransactionType.OUT.getOrder());
        dto.setCustomerId(customer.getId());
        dto.setCreatedById(CreatedBy.CUSTOMER.getOrder());

        return dto;
    }

}
