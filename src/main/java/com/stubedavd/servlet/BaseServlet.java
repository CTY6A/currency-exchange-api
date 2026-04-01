package com.stubedavd.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubedavd.exception.NotFoundException;
import com.stubedavd.listener.ContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class BaseServlet extends HttpServlet {


    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {

        super.init();

        this.objectMapper =
                (ObjectMapper) getServletContext().getAttribute(ContextListener.OBJECT_MAPPER);

        if (this.objectMapper == null) {
            throw new NotFoundException("Object mapper not found");
        }
    }
    protected void sendJson(HttpServletResponse response, int status, Object object) throws IOException {

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(status);

        objectMapper.writeValue(response.getWriter(), object);
    }
}
