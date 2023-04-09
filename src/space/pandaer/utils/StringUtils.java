package space.pandaer.utils;

public class StringUtils {

    public static String uncap(String src){
        char[] charArray = src.toCharArray();
        charArray[0] = Character.toLowerCase(charArray[0]);
        return String.valueOf(charArray);
    }
}
