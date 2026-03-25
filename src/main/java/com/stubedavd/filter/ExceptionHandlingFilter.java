package com.stubedavd.filter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import com.stubedavd.dto.response.ErrorResponseDto;
import com.stubedavd.exception.AlreadyExistException;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.exception.NotFoundException;
import com.stubedavd.exception.ValidationException;

@WebFilter("/*")
public class ExceptionHandlingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            chain.doFilter(request, response);
        } catch (InfrastructureException e) {
            writeError(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (AlreadyExistException e) {
            writeError(httpResponse, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (ValidationException e) {
            writeError(httpResponse, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            writeError(httpResponse, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(status);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), new ErrorResponseDto(message));
    }
}
