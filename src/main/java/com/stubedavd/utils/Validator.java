package com.stubedavd.utils;

import com.stubedavd.exception.ValidationException;

import java.math.BigDecimal;

public class Validator {
    public static void validateCurrency(String name, String code, String sign) throws ValidationException {
        if (name == null || name.isBlank()) {
            throw new ValidationException();
        }

        if (code == null || !code.matches("[A-z]{3}")) {
            throw new ValidationException();
        }

        if (sign == null || sign.isBlank() || sign.length() > 2 ) {
            throw new ValidationException();
        }
    }

    public static void validateExchangeRate(String baseCurrencyCode, String targetCurrencyCode, String rate) throws ValidationException {
        if (baseCurrencyCode == null || !baseCurrencyCode.matches("[A-z]{3}")) {
            throw new ValidationException();
        }

        if (targetCurrencyCode == null || !targetCurrencyCode.matches("[A-z]{3}")) {
            throw new ValidationException();
        }

        try {
            System.out.println(rate);
            new BigDecimal(rate);
        } catch (NullPointerException | NumberFormatException e) {
            throw new ValidationException();
        }
    }
}
