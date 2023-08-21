package az.azercell.customer.repository;

import az.azercell.customer.model.Customer;
import az.azercell.customer.model.Otp;

import java.util.List;


public interface CustomerRepository extends BaseJpaSpecificationRepository<Customer, Long> {

    List<Customer> findByGsmNumber(String gsmNumber);


}
