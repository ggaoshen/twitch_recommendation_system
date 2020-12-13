package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SearchServlet", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String gameId = request.getParameter("game_id");
        if (gameId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        TwitchClient client = new TwitchClient();

        try {
//            response.setContentType("application/json;charset=UTF-8");
//
//            Map<String, List<Item>> items = client.searchItems(gameId);
//            response.getWriter().print( // 写入response
//                    new ObjectMapper().writeValueAsString(items) // 这个是用jackson的mapper，把map convert成json string
//            );

            // 精简到util里
            ServletUtil.writeItemMap(response, client.searchItems(gameId));

        } catch (TwitchException e) {
            throw new ServletException(e);
        }
    }
}
