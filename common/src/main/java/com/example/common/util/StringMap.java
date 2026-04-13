package com.example.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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

    public static String mapMinuteToRelativeTime(Integer totalMinutesFromEpoch) {
        if (totalMinutesFromEpoch == null) {
            return "null";
        }

        int nowTotalMinutes = (int) (Instant.now().getEpochSecond() / 60);

        int diff = nowTotalMinutes - totalMinutesFromEpoch;

        if (diff >= 0 && diff <= 60) {
            if (diff == 0) {
                return "just now";
            }
            return diff + " min ago";
        }

        return mapMinuteToTime(totalMinutesFromEpoch);
    }

    public static String mapMinuteToTime(Integer totalMinutesFromEpoch) {
        if (totalMinutesFromEpoch == null) {
            return "null";
        }

        long totalSeconds = (long) totalMinutesFromEpoch * 60;

        Instant instant = Instant.ofEpochSecond(totalSeconds);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static String mapMinuteOfDayToTime(Integer minuteOfDay) {
        if (minuteOfDay == null) {
            return "null";
        }

        if (minuteOfDay < 0 || minuteOfDay > 1439) {
            return "invalid";
        }

        int hour = minuteOfDay / 60;
        int minute = minuteOfDay % 60;

        return String.format("%02d:%02d", hour, minute);
    }

    public static String mapDateToRelativeLabel(LocalDate targetDate) {

        LocalDate today = LocalDate.now();

        long diff = ChronoUnit.DAYS.between(today, targetDate);

        if (diff == 0) {
            return "Today";
        } else if (diff == -1) {
            return "Yesterday";
        } else if (diff == 1) {
            return "Tomorrow";
        } else {
            // 超出范围 → 返回星期
            return switch (targetDate.getDayOfWeek()) {
                case MONDAY -> "Monday";
                case TUESDAY -> "Tuesday";
                case WEDNESDAY -> "Wednesday";
                case THURSDAY -> "Thursday";
                case FRIDAY -> "Friday";
                case SATURDAY -> "Saturday";
                case SUNDAY -> "Sunday";
            };
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
