package com.example.bridge.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

public class StringMap {
    public static String mapNumberWithUnit(double data, String unit) {
        if (data % 1 == 0) {
            return String.format("%,d " + unit, (int)data);
        } else {
            return String.format("%,.1f " + unit, data);
        }
    }

    public static String mapNumberWithoutUnit(double data) {
        if (data % 1 == 0) {
            return String.format("%,d ", (int)data);
        } else {
            return String.format("%,.1f ", data);
        }
    }

    public static String mapMinuteToRelativeTime(Integer minutes) {
        if (minutes == null) {
            return "null";
        }

        long timeMillis = minutes * 60L * 1000L;

        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timeMillis);

        long diffMinutes = (now.getTimeInMillis() - target.getTimeInMillis()) / (60 * 1000);

        if (diffMinutes < 0) {
            return "刚刚";
        }

        if (diffMinutes == 0) {
            return "刚刚";
        } else if (diffMinutes <= 60) {
            return diffMinutes + " 分钟前";
        }

        long diffDays = (now.getTimeInMillis() / (24L * 60 * 60 * 1000)) -
                (target.getTimeInMillis() / (24L * 60 * 60 * 1000));

        if (diffDays == 0) {
            return String.format("%02d:%02d",
                    target.get(Calendar.HOUR_OF_DAY),
                    target.get(Calendar.MINUTE));
        } else if (diffDays == 1) {
            return "昨天 " + String.format("%02d:%02d",
                    target.get(Calendar.HOUR_OF_DAY),
                    target.get(Calendar.MINUTE));
        } else if (diffDays <= 7) {
            String[] weekDays = {"日","一","二","三","四","五","六"};
            int dayOfWeek = target.get(Calendar.DAY_OF_WEEK);
            return "周" + weekDays[dayOfWeek - 1] + " " +
                    String.format("%02d:%02d",
                            target.get(Calendar.HOUR_OF_DAY),
                            target.get(Calendar.MINUTE));
        } else {
            return String.format("%d-%02d-%02d %02d:%02d",
                    target.get(Calendar.YEAR),
                    target.get(Calendar.MONTH) + 1,
                    target.get(Calendar.DAY_OF_MONTH),
                    target.get(Calendar.HOUR_OF_DAY),
                    target.get(Calendar.MINUTE));
        }
    }

    public static String mapMinuteToTime(Integer minutes) {
        if (minutes == null) {
            return "null";
        }
        int hour = minutes / 60;
        int minute = minutes % 60;
        return String.format("%02d:%02d", hour, minute);
    }

    public static String mapDateToRelativeLabel(LocalDate targetDate) {

        LocalDate today = LocalDate.now();

        long diff = ChronoUnit.DAYS.between(today, targetDate);

        if (diff == 0) {
            return "今日";
        } else if (diff == -1) {
            return "昨日";
        } else if (diff == 1) {
            return "明日";
        } else {
            // 超出范围 → 返回星期
            switch (targetDate.getDayOfWeek()) {
                case MONDAY:
                    return "周一";
                case TUESDAY:
                    return "周二";
                case WEDNESDAY:
                    return "周三";
                case THURSDAY:
                    return "周四";
                case FRIDAY:
                    return "周五";
                case SATURDAY:
                    return "周六";
                case SUNDAY:
                    return "周日";
                default:
                    return "";
            }
        }
    }

    private static final int DAY_START = 6 * 60;   // 360
    private static final int DAY_END = 18 * 60;    // 1080

    public static boolean isDayTime(int minuteOfDay) {
        return minuteOfDay >= DAY_START && minuteOfDay < DAY_END;
    }

    public static int mapStringToSixDigitInt(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());

            int hash = ((digest[0] & 0xff) << 24)
                    | ((digest[1] & 0xff) << 16)
                    | ((digest[2] & 0xff) << 8)
                    | (digest[3] & 0xff);

            hash = Math.abs(hash);

            return hash % 1_000_000;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
