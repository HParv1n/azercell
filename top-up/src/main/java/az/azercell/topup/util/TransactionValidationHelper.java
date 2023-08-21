package az.azercell.topup.util;

import az.azercell.topup.dto.CustomerDTO;
import az.azercell.topup.exceptions.CustomerNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TransactionValidationHelper {

    public static void validateCustomer(CustomerDTO customer) {
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found.");
        }
    }
}
