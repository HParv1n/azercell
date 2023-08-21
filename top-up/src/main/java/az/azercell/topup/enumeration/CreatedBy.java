package az.azercell.topup.enumeration;

import java.util.Arrays;

public enum CreatedBy
{
    SYSTEM(0),
    CUSTOMER(1);

    private final int order;
    CreatedBy(int order)
    {
        this.order = order;
    }

    public int getOrder()
    {
        return order;
    }
    public static CreatedBy of(int order)
    {
        return Arrays.stream(CreatedBy.values())
                .filter(t -> t.getOrder() == order)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Created By not found: " + order));
    }

}
