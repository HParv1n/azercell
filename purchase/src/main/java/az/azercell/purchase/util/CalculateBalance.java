package az.azercell.purchase.util;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CalculateBalance {

    public static BigDecimal calculateNewBalance(BigDecimal currentBalance, double amount) {
        return currentBalance.subtract(BigDecimal.valueOf(amount));
    }

}
