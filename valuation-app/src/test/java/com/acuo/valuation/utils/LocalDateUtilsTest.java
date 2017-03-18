package com.acuo.valuation.utils;

import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalDateUtilsTest {

    @Test
    public void testMinusOneDayFromMonday() {
        LocalDate monday = LocalDate.of(2017, 3, 13);
        LocalDate friday = LocalDateUtils.minus(monday, 1);
        assertThat(friday).isEqualTo(LocalDate.of(2017, 3, 10));
    }

    @Test
    public void testMinusOneDayFromTuesday() {
        LocalDate monday = LocalDate.of(2017, 3, 14);
        LocalDate friday = LocalDateUtils.minus(monday, 1);
        assertThat(friday).isEqualTo(LocalDate.of(2017, 3, 13));
    }

}