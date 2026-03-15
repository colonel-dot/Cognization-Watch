package util;

public class Util {
    public static String map(double data, String unit) {
        if (data % 1 == 0) {
            return String.format("%,d " + unit, (int)data);
        } else {
            return String.format("%,.1f " + unit, data);
        }
    }
}
