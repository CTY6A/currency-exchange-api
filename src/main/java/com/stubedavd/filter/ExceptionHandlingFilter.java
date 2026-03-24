package com.stubedavd.filter;

import com.stubedavd.exception.AlreadyExistException;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.exception.ValidationException;
import com.stubedavd.model.response.ErrorResponse;
import com.stubedavd.utils.ResponseHelper;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class ExceptionHandlingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            chain.doFilter(request, response);
        } catch (InfrastructureException e) {
            writeError(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (AlreadyExistException e) {
            writeError(httpResponse, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (ValidationException e) {
            writeError(httpResponse, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) {
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(status);
        new ResponseHelper(response, new ErrorResponse(message));
    }
}
