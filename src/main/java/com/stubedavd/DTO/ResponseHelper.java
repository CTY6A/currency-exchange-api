package com.stubedavd.DTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ResponseHelper {
    public ResponseHelper(HttpServletResponse response, Object object) {
        response.setContentType("application/json; charset=UTF-8");
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(object);
            PrintWriter out = response.getWriter();
            out.println(json);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
