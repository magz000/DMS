package ph.edu.tip.app.dms.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Encrypt {

    public static String protectString(String stringToProtect) {
        String[] encArray = {
                "F5uCMoLzy4fBKHEqdGXOoJrGkG6FcsTFNC8hY2rw6aDmPvQHpu6gXTToO7Y6oJrGkG6FcsTFNC8hY2rw5oET3XZH7QO080fyIoROcpNqwPLHgSBcVXegj6kj",
                "oJrGkG6FcsTFNC8hY2rw5oET3XZH7QO080fyIoROcpNqwPLHgSBcVXegj6kjcpNqwPLHgSBcVXegj6kj6aDmPvQHpu6gXTToO7Y6oJrGkG6FcsTFNC8hY2rw",
                "cpNqwPLHgSBcVXegj6kj6aDmPvQHpu6gXTToO7Y6oJrGkG6FcsTFNC8hY2rw6aDmPvQHpu6gXTToO7Y6F5uCMoLzy4fBKHEqdGXO5oET3XZH7QO080fyIoRO",
                "6aDmPvQHpu6gXTToO7Y6F5uCMoLzy4fBKHEqdGXO5oET3XZH7QO080fyIoRO5oET3XZH7QO080fyIoROcpNqwPLHgSBcVXegj6kjF5uCMoLzy4fBKHEqdGXO",
                "5oET3XZH7QO080fyIoROcpNqwPLHgSBcVXegj6kjF5uCMoLzy4fBKHEqdGXOF5uCMoLzy4fBKHEqdGXOoJrGkG6FcsTFNC8hY2rw6aDmPvQHpu6gXTToO7Y6"
        };
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy", Locale.US);
        String dateToString = formatter.format(date);
        int intTotal = 0;
        for (int i = 0; i < dateToString.length(); i++) {
            int intStr = Integer.parseInt(dateToString.charAt(i) + "");
            intTotal += intStr;
        }
        int intCheckSum = intTotal % 5;
        String encryptedStr = "";
        String encString = getEncStringForEncryption(stringToProtect, encArray[intCheckSum], encArray[intCheckSum]);
        char aVal;
        char bVal;
        for (int i = 0; i < stringToProtect.length(); i++) {
            aVal = stringToProtect.charAt(i);
            bVal = encString.charAt(i);
            int cVal = (int) aVal + (int) bVal;
            String intToStr = String.format("%03d", cVal);
            encryptedStr += intToStr;
        }
        return encryptedStr;
    }

    private static String getEncStringForEncryption(String stringToProtect, String encString, String originalEncString) {
        if (stringToProtect.length() <= encString.length()) return encString;
        else
            return getEncStringForEncryption(stringToProtect, encString + originalEncString, originalEncString);
    }

    public static String unprotectString(String protectedString) {
        String[] encArray = {
                "F5uCMoLzy4fBKHEqdGXOoJrGkG6FcsTFNC8hY2rw6aDmPvQHpu6gXTToO7Y6oJrGkG6FcsTFNC8hY2rw5oET3XZH7QO080fyIoROcpNqwPLHgSBcVXegj6kj",
                "oJrGkG6FcsTFNC8hY2rw5oET3XZH7QO080fyIoROcpNqwPLHgSBcVXegj6kjcpNqwPLHgSBcVXegj6kj6aDmPvQHpu6gXTToO7Y6oJrGkG6FcsTFNC8hY2rw",
                "cpNqwPLHgSBcVXegj6kj6aDmPvQHpu6gXTToO7Y6oJrGkG6FcsTFNC8hY2rw6aDmPvQHpu6gXTToO7Y6F5uCMoLzy4fBKHEqdGXO5oET3XZH7QO080fyIoRO",
                "6aDmPvQHpu6gXTToO7Y6F5uCMoLzy4fBKHEqdGXO5oET3XZH7QO080fyIoRO5oET3XZH7QO080fyIoROcpNqwPLHgSBcVXegj6kjF5uCMoLzy4fBKHEqdGXO",
                "5oET3XZH7QO080fyIoROcpNqwPLHgSBcVXegj6kjF5uCMoLzy4fBKHEqdGXOF5uCMoLzy4fBKHEqdGXOoJrGkG6FcsTFNC8hY2rw6aDmPvQHpu6gXTToO7Y6"


        };
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy", Locale.US);
        String dateToString = formatter.format(date);
        int intTotal = 0;
        for (int i = 0; i < dateToString.length(); i++) {
            int intStr = Integer.parseInt(dateToString.charAt(i) + "");
            intTotal += intStr;
        }
        int intCheckSum = intTotal % 5;
        String plainStr = "";
        String encString = getEncStringForDecryption(protectedString, encArray[intCheckSum], encArray[intCheckSum]);
        char bVal;
        int j = 0;
        for (int i = 0; i < protectedString.length(); i += 3) {
            int aVal = Integer.parseInt(protectedString.substring(i, i + 3));
            bVal = encString.charAt(j);
            j++;
            int cVal = aVal - (int) bVal;
            String intToStr = Character.toString((char) cVal);
            plainStr += intToStr;
        }
        return plainStr;
    }

    private static String getEncStringForDecryption(String protectedString, String encString, String originalEncString) {
        if (protectedString.length() / 3 <= encString.length()) return encString;
        else
            return getEncStringForDecryption(protectedString, encString + originalEncString, originalEncString);
    }

}