package az.azercell.refund.enumeration;

import java.util.Arrays;

public enum TransactionType
{

    IN(0),
    OUT(1),
    REFUND(2);

    private final int order;
    TransactionType(int order)
    {
        this.order = order;
    }

    public int getOrder()
    {
        return order;
    }
    public static TransactionType of(int order)
    {
        return Arrays.stream(TransactionType.values())
                .filter(t -> t.getOrder() == order)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Transaction type not found: " + order));
    }

}
