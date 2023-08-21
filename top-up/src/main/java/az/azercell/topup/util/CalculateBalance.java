package az.azercell.topup.util;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CalculateBalance {

    public static BigDecimal calculateNewBalance(BigDecimal currentBalance, double amount) {
        return currentBalance.add(BigDecimal.valueOf(amount));
    }

}
