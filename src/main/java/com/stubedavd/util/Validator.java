package com.stubedavd.util;

import com.stubedavd.exception.ValidationException;

import java.math.BigDecimal;

public final class Validator {

    public static void validateCurrency(String name, String code, String sign) {

        if (name == null || name.isBlank()) {
            throw new ValidationException("Name is invalid");
        }

        if (code == null || !code.matches("[A-Za-z]{3}")) {
            throw new ValidationException("Code is invalid");
        }

        if (sign == null || sign.isBlank() || sign.length() > 2 ) {
            throw new ValidationException("Sign is invalid");
        }
    }

    public static void validateExchangeRate(String baseCurrencyCode, String targetCurrencyCode, String rate) {

        if (baseCurrencyCode == null || !baseCurrencyCode.matches("[A-Za-z]{3}")) {
            throw new ValidationException("Base currency code is invalid");
        }

        if (targetCurrencyCode == null || !targetCurrencyCode.matches("[A-Za-z]{3}")) {
            throw new ValidationException("Target currency code is invalid");
        }

        try {
            System.out.println(rate);
            new BigDecimal(rate);
        } catch (NullPointerException | NumberFormatException e) {
            throw new ValidationException("Rate is invalid");
        }
    }

    public static void validateOneCodePath(String pathInfo) {

        if (pathInfo == null || pathInfo.equals("/")) {
            throw new ValidationException("A required form field is missing");
        }
    }

    public static final int TWO_CODES_AND_SLASH = 7;

    public static void validateTwoCodesPath(String pathInfo) {

        if (pathInfo == null || pathInfo.length() != TWO_CODES_AND_SLASH) {
            throw new ValidationException("A required form field is missing");
        }
    }

    public static void validateRateParameter(String rateParameter) {

        if (rateParameter == null || rateParameter.isBlank()) {
            throw new ValidationException("Rate parameter is invalid");
        }
    }
}
