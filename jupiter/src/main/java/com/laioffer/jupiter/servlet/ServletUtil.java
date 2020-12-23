package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Item;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class ServletUtil {
    public static void writeItemMap(HttpServletResponse response, Map<String, List<Item>> itemMap)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8"); // 存json格式，字符集utf8可以handle中文等非英文char
        response.getWriter().print( // 写入response
                new ObjectMapper().writeValueAsString(itemMap) // 这个是用jackson的mapper，把map convert成json string
        );
    }

    public static String encryptPassword(String userId, String password) throws IOException {
        return DigestUtils.md5Hex(userId + DigestUtils.md5Hex(password)).toLowerCase();
    }

    public static <T> T readRequestBody(Class<T> cls, HttpServletRequest request) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(request.getReader(), cls); // request.getReader()是request body的stream
        } catch (JsonParseException | JsonMappingException e) {
            return null;
        }
    }
}
