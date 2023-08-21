package az.azercell.purchase.util;

import az.azercell.purchase.dto.CustomerDTO;
import az.azercell.purchase.exceptions.CustomerNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TransactionValidationHelper {

    public static void validateCustomer(CustomerDTO customer) {
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found.");
        }
    }
}
