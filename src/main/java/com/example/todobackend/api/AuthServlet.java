package com.example.todobackend.api;

import com.example.todobackend.dto.userDTO;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "UserServlet", value = "/auth/*")
public class AuthServlet extends HttpServlet {


    @Resource(name = "java:comp/env/jdbc/pool4todo")
    private volatile DataSource pool;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*1. validate */
        if (request.getContentType() == null ||
                !request.getContentType().toLowerCase().startsWith("application/json")) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        try {
            Jsonb jsonb = JsonbBuilder.create();
            userDTO user = jsonb.fromJson(request.getReader(), userDTO.class);

            if (user.getUserName() == null || !user.getUserName().matches("[A-Za-z0-9 ]+")) {
                throw new ValidationException("Invalid User name");
            } else if (user.getPassWord() == null || !user.getPassWord().matches("[A-Za-z0-9 ]+")) {
                throw new ValidationException("Invalid password");
            }



            try (Connection connection = pool.getConnection()) {

                /*check it is a duplicate*/
                PreparedStatement stm = connection.prepareStatement("SELECT * FROM user where username=? and password=?");
                stm.setString(1,user.getUserName());
                stm.setString(2,user.getPassWord());
                ResultSet rst = stm.executeQuery();
                if (rst.next()){
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    System.out.println("user verified");
                    return;
                }else {
                    response.sendError(HttpServletResponse.SC_CONFLICT, "user verification failed");
                }



            }
        } catch (JsonbException | ValidationException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    (e instanceof JsonbException) ? "Invalid JSON" : e.getMessage());
        } catch (Throwable t){
            t.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
