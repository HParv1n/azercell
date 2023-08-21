package az.azercell.topup.dto;

import az.azercell.topup.enumeration.CreatedBy;
import az.azercell.topup.enumeration.TransactionType;
import az.azercell.topup.model.Transaction;
import az.azercell.topup.model.base.BaseDTO;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO extends BaseDTO {

    private Long id;


    private Long customerId;

    private Integer transactionTypeId;

    @Transient
    private TransactionType transactionType;

    private BigDecimal amount;

    private Integer createdById;

    private Long parentId;

    @Transient
    private CreatedBy createdBy;

    private BigDecimal beforeAmount;

    private BigDecimal afterAmount;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operatedAt;

    public TransactionDTO(Transaction entity)
    {
        this.id = entity.getId();
        this.amount = entity.getAmount();
        this.afterAmount = entity.getAfterAmount();
        this.beforeAmount = entity.getBeforeAmount();
        this.customerId = entity.getCustomerId();
        this.transactionTypeId = entity.getTransactionTypeId();
        this.transactionType = TransactionType.of(entity.getTransactionTypeId());
        this.parentId = entity.getParentId();
        this.createdById = entity.getCreatedById();
        this.createdBy = CreatedBy.of(entity.getCreatedById());
        this.operatedAt = entity.getOperatedAt();

    }

}
