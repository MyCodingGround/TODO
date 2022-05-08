package com.example.todobackend.api;

import com.example.todobackend.dto.userDTO;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "UserServlet", value = "/users/*")
public class UserServlet extends HttpServlet {


    @Resource(name = "java:comp/env/jdbc/pool4todo")
    private volatile DataSource pool;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("dopost working");
        doSaveOrUpdate(request,response);

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doSaveOrUpdate(req,resp);
    }

    private void doSaveOrUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException {

        if (req.getContentType() == null ||
                !req.getContentType().toLowerCase().startsWith("application/json")) {
            res.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        String method = req.getMethod();
        String pathInfo = req.getPathInfo();


      if (method.equals("POST") && (pathInfo != null && !pathInfo.equals("/"))) {
        res.sendError(HttpServletResponse.SC_NOT_FOUND,"User not fount in servlet");
        return;
    } else if (method.equals("PUT") && !(pathInfo != null &&
            pathInfo.substring(1).matches("[A-Za-z ]"))) {
        res.sendError(HttpServletResponse.SC_NOT_FOUND, "User does not exist");
        return;
    }




        try {
            Jsonb jsonb = JsonbBuilder.create();
            userDTO user = jsonb.fromJson(req.getReader(), userDTO.class);
            if (method.equals("POST") &&  //|| !user.getUserName().matches("[A-Za-z ]")
                    (user.getUserName() == null )) {
                throw new ValidationException("Invalid User Name");
            } else if (user.getGmail() == null || !user.getGmail().matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")) {
                throw new ValidationException("Invalid gmail");
            } else if (user.getPassWord() == null) {
                throw new ValidationException("add the password ");
            }

            if (method.equals("PUT")){
                user.setUserName(pathInfo.replaceAll("[/]", ""));
            }

            try (Connection connection = pool.getConnection()) {
                PreparedStatement stm = connection.prepareStatement("SELECT * FROM user WHERE username=?");
                stm.setString(1,  user.getUserName());
                ResultSet rst = stm.executeQuery();

                if (rst.next()) {
                    if (method.equals("POST")){
                        res.sendError(HttpServletResponse.SC_CONFLICT, "User already exists");
                    }else{
                        stm =  connection.prepareStatement("UPDATE user SET password=?, gmail=? WHERE username=?");
                        stm.setString(1, user.getPassWord());
                        stm.setString(2, user.getGmail());
                        stm.setString(3, user.getUserName());
                        if (stm.executeUpdate() != 1){
                            throw new RuntimeException("Failed to update the User");
                        }
                        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                }else{
                    stm = connection.prepareStatement("INSERT INTO user (username, password, gmail) VALUES (?,?,?)");
                    stm.setString(1, user.getUserName());
                    stm.setString(2, user.getPassWord());
                    stm.setString(3, user.getGmail());
                    if (stm.executeUpdate() != 1) {
                        throw new RuntimeException("Failed to register the user");
                    }
                    res.setStatus(HttpServletResponse.SC_CREATED);
                }
            }

        } catch (JsonbException | ValidationException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    (e instanceof JsonbException) ? "Invalid JSON" : e.getMessage());
        } catch (Throwable t){
            t.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("do delete");
        if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Unable to delete all user yet");
            return;
        } else if (req.getPathInfo() != null &&
                !req.getPathInfo().substring(1).matches("[A-Za-z0-9 ]+")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "user not found");
            System.out.println(req.getPathInfo().substring(1).matches("[A-Za-z ]+"));
            return;
        }

        String username = req.getPathInfo().replaceAll("[/]", "");

        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.
                    prepareStatement("SELECT * FROM user WHERE username=?");
            stm.setString(1, username);
            ResultSet rst = stm.executeQuery();

            if (rst.next()) {

                stm = connection.prepareStatement("DELETE FROM user WHERE username=?");
                stm.setString(1, username);
                if (stm.executeUpdate() != 1) {
                    throw new RuntimeException("Failed to delete the user");
                }
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "user not found");
            }
        } catch (SQLException | RuntimeException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

}
