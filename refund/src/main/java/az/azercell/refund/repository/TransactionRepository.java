package az.azercell.refund.repository;

import az.azercell.refund.enumeration.TransactionType;
import az.azercell.refund.model.Transaction;

import java.util.List;

public interface TransactionRepository extends BaseJpaSpecificationRepository<Transaction,Long>{

    List<Transaction> findByCustomerId(Long customerId);

}
