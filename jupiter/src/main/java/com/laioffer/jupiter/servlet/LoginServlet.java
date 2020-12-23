package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.LoginRequestBody;
import com.laioffer.jupiter.entity.LoginResponseBody;
import com.laioffer.jupiter.entity.User;
import com.mysql.cj.Session;
import org.apache.http.protocol.ResponseServer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LoginRequestBody body = ServletUtil.readRequestBody(LoginRequestBody.class, request);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String userName;
        MySQLConnection connection = null;
        try {
            connection = new MySQLConnection();
            String userId = body.getUserId();
            String password = ServletUtil.encryptPassword(body.getUserId(), body.getPassword());
            userName = connection.verifyLogin(userId, password);
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            connection.close();
        }

        if (!userName.isEmpty()) { // 登录成功, 需要创建session
            // session不需要加密，因为只返回给前端session id，没别的，所以没有安全性问题
            HttpSession session = request.getSession(); //如果有session就返回现有session，没有就create
            session.setAttribute("user_id", body.getUserId());
            session.setAttribute("user_name", userName);
            session.setMaxInactiveInterval(600); // 设置session，inactive 600s以上就expire

            LoginResponseBody loginResponseBody = new LoginResponseBody(body.getUserId(), userName);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(new ObjectMapper().writeValueAsString(loginResponseBody));

        } else { // 登录失败， 返回error 401 unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }



}
