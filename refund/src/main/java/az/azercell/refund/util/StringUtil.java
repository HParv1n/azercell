package az.azercell.refund.util;


public interface StringUtil
{
    static boolean isEmptyOrNull(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
