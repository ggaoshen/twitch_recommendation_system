package com.laioffer.jupiter.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class TwitchClient {
    // 为什么用普通java class而不是Servlet？因为我们访问twitch api，我们不handle request了
    private static final String TOKEN = "Bearer fo1748w2del5svoxlkwrposm7dd6va";
    private static final String CLIENT_ID = "2mv0z7zok3b27qpt9oqaxsxaj4prdx";
    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;

    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    private static final int DEFAULT_SEARCH_LIMIT = 20;


    private String buildGameURL(String url, String gameName, int limit) {
        if (gameName.equals("")) { // TOP GAME URL
            return String.format(url, limit); // replaces %s with number of games
        } else { // SEARCH SPECIFIC GAME
            try {
                gameName = URLEncoder.encode(gameName, "UTF-8"); // ENCODE: 空格改%20
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format(url, gameName);
        }
    }

    private String buildSearchURL(String url, String gameId, int limit) {
        try {
            gameId = URLEncoder.encode(gameId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format(url, gameId, limit); // 第一个%s替换String gameId，第二个换limit
    }

    private String searchTwitch(String url) throws TwitchException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        ResponseHandler<String> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status != 200) {
                throw new TwitchException("Failed to get data from Twitch");
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new TwitchException("Failed to get data from Twitch");
            }
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            return obj.getJSONArray("data").toString();
        };

        try {
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", TOKEN);
            request.setHeader("Client-Id", CLIENT_ID);
            return httpclient.execute(request, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to get data from Twitch");
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private List<Game> getGameList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper(); // convert string to json object
        try {
            // 如果json格式和Game constructor定义不符，会有exception需要handle
            // 或在Game class里加以下：
            // @JsonIgnoreProperties(ignoreUnknown = true) // ignore extra key
            // @JsonInclude(JsonInclude.Include.NON_NULL) // 忽略null
            // @JsonDeserialize(builder = Game.Builder.class) //使用builder class而不是Game自己的constructor
            Game[] games = mapper.readValue(data, Game[].class);
            return Arrays.asList(games);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API");
        }
    }

    public List<Game> topGames(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        String url = buildGameURL(TOP_GAME_URL, "", limit);
        String responseBody = searchTwitch(url);
        return getGameList(responseBody);
    }

    public Game searchGame(String gameName) throws TwitchException {
        List<Game> gameList = getGameList(searchTwitch(buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0)));
        if (gameList.size() != 0) { // only return one result
            return gameList.get(0);
        }
        return null;
    }

    private List<Item> getItemList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper(); // jackson的mapper
        try {
            return Arrays.asList(mapper.readValue(data, Item[].class)); // map json String到Item[]
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse item data from Twitch API");
        }
    }

    private List<Item> searchStreams(String gameId, int limit) throws TwitchException {
        String url = buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit);
        String returnedJsonString = searchTwitch(url); // 返回的search result， JSON格式
        List<Item> streams = getItemList(returnedJsonString); // 用jackson把返回的json format string转成java Item class
        for (Item item : streams) {
            item.setType(ItemType.STREAM);
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
        }
        return streams;
    }

    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        List<Item> clips = getItemList(searchTwitch(buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : clips) {
            item.setType(ItemType.CLIP);
        }
        return clips;
    }

    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        List<Item> videos = getItemList(searchTwitch(buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : videos) {
            item.setType(ItemType.VIDEO);
        }
        return videos;
    }

    public List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = Collections.emptyList();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break;
            case VIDEO:
                items = searchVideos(gameId, limit);
                break;
            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }

        // Update gameId for all items. GameId is used by recommendation function
        for (Item item : items) {
            item.setGameId(gameId);
        }
        return items;
    }

    public Map<String, List<Item>> searchItems(String gameId) throws TwitchException {
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) { // list所有Enum ItemType的值：video，stream，clip
            itemMap.put(type.toString(), searchByType(gameId, type, DEFAULT_SEARCH_LIMIT));
        }
        return itemMap;
    }

}
