package com.stubedavd.DTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubedavd.DAO.CurrencyDAO;
import com.stubedavd.models.Currency;
import com.stubedavd.models.ErrorResponse;
import com.stubedavd.servlets.CurrenciesServlet;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            CurrencyDAO dao = new CurrencyDAO();
            System.out.println(dao.save(new Currency(1, "", "", "")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
