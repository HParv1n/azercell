package az.azercell.topup.service;

import az.azercell.topup.client.CustomerMicroserviceClient;
import az.azercell.topup.dto.CustomerDTO;
import az.azercell.topup.dto.TransactionDTO;
import az.azercell.topup.enumeration.CreatedBy;
import az.azercell.topup.enumeration.TransactionType;
import az.azercell.topup.exceptions.CustomerNotFoundException;
import az.azercell.topup.exceptions.InvalidRefundAmountException;
import az.azercell.topup.generic.GenericServiceImpl;
import az.azercell.topup.model.Transaction;
import az.azercell.topup.repository.TransactionRepository;
import az.azercell.topup.util.JwtTokenUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static az.azercell.topup.util.CalculateBalance.calculateNewBalance;
import static az.azercell.topup.util.JwtTokenUtil.validateToken;
import static az.azercell.topup.util.TransactionValidationHelper.validateCustomer;


@Service
public class TransactionService extends GenericServiceImpl<TransactionDTO, Transaction> {
    private final TransactionRepository repository;
    private final CustomerMicroserviceClient customerMicroserviceClient;

    public TransactionService(TransactionRepository repository, CustomerMicroserviceClient customerMicroserviceClient) {
        super(repository, TransactionDTO::new, TransactionService::dtoToEntity);
        this.repository = repository;
        this.customerMicroserviceClient = customerMicroserviceClient;
    }


    public String addFunds(String jwtToken, double amount) {

        validateToken(jwtToken);
        validateTopUpAmount(amount);
        String gsmNumber = extractPhoneNumberFromToken(jwtToken);
        CustomerDTO customer = getCustomerByGsmNumber(gsmNumber);
        validateCustomer(customer);
        BigDecimal newBalance = calculateNewBalance(customer.getBalance(), amount);
        saveTransaction(customer, amount, newBalance);
        updateCustomerBalance(customer, newBalance);

        return "Fund successful. New balance: " + newBalance;
    }


    protected void validateTopUpAmount(double amount) {

        if (amount < 0.01 || amount > 1000) {
            throw new InvalidRefundAmountException("Invalid amount for Top-Up.");
        }
    }
    private String extractPhoneNumberFromToken(String jwtToken) {
        return JwtTokenUtil.extractPhoneNumber(jwtToken);
    }
    @Override
    public Transaction save(TransactionDTO dto) {
        Transaction entity = dtoToEntity(dto, Optional.empty());
        Transaction saved = repository.save(entity);
        return saved;
    }

    protected CustomerDTO getCustomerByGsmNumber(String gsmNumber) {
        CustomerDTO customer = customerMicroserviceClient.getCustomerByGsmNumber(gsmNumber);
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found.");
        }
        return customer;
    }

    protected void saveTransaction(CustomerDTO customer, double amount, BigDecimal newBalance) {
        TransactionDTO transactionDTO = customerToTransactionDTO(customer, Optional.empty(), amount, newBalance);
        save(transactionDTO);
    }

    protected void updateCustomerBalance(CustomerDTO customer, BigDecimal newBalance) {
        customer.setBalance(newBalance);
        customerMicroserviceClient.updateCustomer(customer, customer.getId());
    }

    protected static Transaction dtoToEntity(TransactionDTO dto, Optional<Transaction> existingEntity) {
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
        dto.setTransactionTypeId(TransactionType.IN.getOrder());
        dto.setCustomerId(customer.getId());
        dto.setCreatedById(CreatedBy.CUSTOMER.getOrder());

        return dto;
    }

}
