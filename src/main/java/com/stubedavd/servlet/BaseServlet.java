package com.stubedavd.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class BaseServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    protected void sendJson(HttpServletResponse response, int status, Object object) throws IOException {

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), object);
    }
}
