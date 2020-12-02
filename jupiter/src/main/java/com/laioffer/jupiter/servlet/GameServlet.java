package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;
import org.json.JSONObject;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GameServlet", urlPatterns = {"/game"})
public class GameServlet extends HttpServlet {
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        // IOUtils.toString() 把前端传进来的data stream存成string
//        JSONObject jsonRequest = new JSONObject(IOUtils.toString(request.getReader())); // 内部是个hashmap
//        String name = jsonRequest.getString("name");
//        String developer = jsonRequest.getString("developer");
//        String release_time = jsonRequest.getString("release_time");
//        String website = jsonRequest.getString("website");
//        float price = jsonRequest.getFloat("price");
//
//        // 正常应该写入database
//        System.out.println("Name is: "+ name);
//        System.out.println("Developer is: "+ developer);
//        System.out.println("Release Date is: "+release_time);
//        System.out.println("Websiet is: "+website);
//        System.out.println("Price is : "+price);
//
//        response.setContentType("application/json");
//        JSONObject jsonResponse = new JSONObject();
//        jsonResponse.put("status", "ok");
//        response.getWriter().print(jsonResponse);
//
//    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        String requestMethod = request.getMethod(); // CONFIRM IF REQUEST METHOD IS "GET"/"POST"

//        String gameName = request.getParameter("gamename"); // 只能读取URL里面的parameter
//        response.getWriter().print("Game is: " + gameName);

        // define json object for message body
//        response.setContentType("application/json"); // 机器注释
//        Game.GameBuilder builder = new Game.GameBuilder();
//        Game game = builder.build();

//        JSONObject obj = new JSONObject(); // org.json 包可以直接生成json格式的output string
//        // 不用org.JSON包，需要 String responseBody = "/"name/":/"World of Warcraft/"..."
//        obj.put("name", game.getName());
//        obj.put("developer", game.getDeveloper());
//        obj.put("release_time", "Feb 11 2005");
//        obj.put("website", "http://www.worldofwarcraft.com");
//        obj.put("price", 49.99);
//        response.getWriter().print(game);
//
//         怎么把java的object直接转化成json格式，field name当作key？
//         用library： Jackson
//        response.setContentType("application/json");
//        ObjectMapper mapper = new ObjectMapper();
//        Game game = new Game("World of Warcraft", "Blizzard Entertainment", "Feb 11, 2005", "https://www.worldofwarcraft.com", 49.99);
//        response.getWriter().print(mapper.writeValueAsString(game));

        String gameName = request.getParameter("game_name");
        TwitchClient client = new TwitchClient();

        response.setContentType("application/json;charset=UTF-8");
        try {
            if (gameName != null) {
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.searchGame(gameName)));
            } else {
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.topGames(0)));
            }
        } catch (TwitchException e) {
            throw new ServletException(e);
        }

    }
}
