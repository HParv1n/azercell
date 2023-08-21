package az.azercell.refund.model;


import az.azercell.refund.enumeration.CreatedBy;
import az.azercell.refund.enumeration.TransactionType;
import az.azercell.refund.model.base.BaseEntity;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "parent_id")
    private Long parentId;

    private Integer transactionTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    @Transient
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private Integer createdById;

    @Enumerated(EnumType.STRING)
    @Column(name = "created_by", nullable = false)
    @Transient
    private CreatedBy createdBy;

    @Column(name = "before_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal beforeAmount;

    @Column(name = "after_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal afterAmount;

    @Column(name = "operated_at", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operatedAt;

}