package az.azercell.refund.util;

import az.azercell.refund.dto.CustomerDTO;

import az.azercell.refund.exceptions.CustomerNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TransactionValidationHelper {

    public static void validateCustomer(CustomerDTO customer) {
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found.");
        }
    }
}
