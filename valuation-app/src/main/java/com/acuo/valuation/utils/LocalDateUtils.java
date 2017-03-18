package com.acuo.valuation.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class LocalDateUtils {

    private LocalDateUtils() {}

    public static LocalDate add(LocalDate date, int workdays) {
        if (workdays < 1) {
            return date;
        }

        LocalDate result = date;
        int addedDays = 0;
        while (addedDays < workdays) {
            result = result.plusDays(1);
            if (!(result.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    result.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                ++addedDays;
            }
        }

        return result;
    }

    public static LocalDate minus(LocalDate date, int workdays) {
        if (workdays < 1) {
            return date;
        }

        LocalDate result = date;
        int removedDays = 0;
        while (removedDays < workdays) {
            result = result.minusDays(1);
            if (!(result.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    result.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                ++removedDays;
            }
        }
        return result;
    }
}
