package com.stubedavd.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubedavd.DAO.CurrencyDAO;
import com.stubedavd.DTO.ResponseHelper;
import com.stubedavd.models.Currency;
import com.stubedavd.models.ErrorResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse error = new ErrorResponse("A required form field is missing");
            new ResponseHelper(resp, error);
        } else {
            String code = pathInfo.substring(1);
            try {
                CurrencyDAO dao = new CurrencyDAO();
                Currency currency = dao.findByCode(code);
                if (currency == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    ErrorResponse error = new ErrorResponse("Currency not found");
                    new ResponseHelper(resp, error);
                } else {
                    new ResponseHelper(resp, currency);
                }
            } catch (IOException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse error = new ErrorResponse("Database is unavailable");
                new ResponseHelper(resp, error);
            }
        }
    }
}
