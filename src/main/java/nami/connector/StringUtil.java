package nami.connector;

public class StringUtil {

    public static String fillZeroes(String number, int length) {
        StringBuilder result = new StringBuilder(number);
        while (result.length() < length)
            result.append("0");
        return result.toString();
    }
}
