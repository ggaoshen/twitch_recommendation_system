package com.laioffer.jupiter.recommendation;

import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ItemRecommender {
    private static final int DEFAULT_GAME_LIMIT = 3;
    private static final int DEFAULT_PER_GAME_RECOMMENDATION_LIMIT = 10;
    private static final int DEFAULT_TOTAL_RECOMMENDATION_LIMIT = 20;

    private List<Item> recommendByTopGames(ItemType type, List<Game> topGames)
            throws RecommendationException {
        // recommend a list of items based on top games and desired itemtype

        List<Item> recommendedItems = new ArrayList<>();
        TwitchClient client = new TwitchClient();

        outerloop: // 可以直接break outerloop
        for (Game game: topGames) {
            List<Item> items;
            try {
                items = client.searchByType(
                        game.getId(),
                        type,
                        DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch(TwitchException e) {
                throw new RecommendationException("Failed to recommend by top games");
            }

            for (Item item : items) {
                if (recommendedItems.size() >= DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    break outerloop; // 可以直接break outerloop
                }
                recommendedItems.add(item);
            }

        }
        return recommendedItems;
    }

    private List<Item> recommendByFavoriteHistory(Set<String> favoritedItemIds,
                                                  List<String> favoriteGameIds,
                                                  ItemType type)
            throws RecommendationException {
        List<Item> recommendedItems = new ArrayList<>();
        TwitchClient client = new TwitchClient();

        // favoriteGameIds -> [1234, 1234, 2345] -> {1234:2, 2345:1} // map然后order by count
        // 可以用遍历的方法
        // 这里用stream的方法，更简便
        Map<String, Long> favoriteGameIdsByCount = favoriteGameIds.parallelStream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()));
        // Function.identity() 相当于  str -> str, 输入是啥输出也是啥，返回自己，一个String

        // 然后需要排序: 搞成Entry List，然后用List.SORT
        List<Map.Entry<String, Long>> sortedFavoriteGameIdByCount =
                new ArrayList<>(favoriteGameIdsByCount.entrySet());

        sortedFavoriteGameIdByCount.sort( // lambda comparator
                (e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));

        if (sortedFavoriteGameIdByCount.size() > DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
            sortedFavoriteGameIdByCount = sortedFavoriteGameIdByCount.subList(
                    0, DEFAULT_TOTAL_RECOMMENDATION_LIMIT
            );
        }

        // recommend
        outerloop: // 可以直接break outerloop
        for (Map.Entry<String, Long> entry : sortedFavoriteGameIdByCount) {
            List<Item> items;
            try {
                items = client.searchByType(
                        entry.getKey(),
                        type,
                        DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch(TwitchException e) {
                throw new RecommendationException("Failed to recommend by top games");
            }

            for (Item item : items) {
                if (recommendedItems.size() >= DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    break outerloop; // 可以直接break outerloop
                }
                if (!favoritedItemIds.contains(item.getId())) {
                    recommendedItems.add(item);
                }
            }

        }
        return recommendedItems;
    }

    public Map<String, List<Item>> recommendItemsByUser(String userId) throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        Set<String> favoriteItemIds; // [aaaa,bbbb,cccc,dddd]
        Map<String, List<String>> favoriteGameIds; // {"video": [1234, 1234, 2345], "stream": [2345], "clip": []} // gameId可能重复
        MySQLConnection connection = null;
        try {
            connection = new MySQLConnection();
            favoriteItemIds = connection.getFavoriteItemIds(userId);
            favoriteGameIds = connection.getFavoriteGameIds(favoriteItemIds); // <ItemType, list of item ids>
        } catch (MySQLException e) {
            throw new RecommendationException("Failed to get user favorite history for recommendation");
        } finally {
            connection.close();
        }

        for (Map.Entry<String, List<String>> entry : favoriteGameIds.entrySet()) {
            if (entry.getValue().size() == 0) { // if no favorite for the ItemType, recommend by top game
                TwitchClient client = new TwitchClient();
                List<Game> topGames;
                try {
                    topGames = client.topGames(DEFAULT_GAME_LIMIT);
                } catch (TwitchException e) {
                    throw new RecommendationException("Failed to get game data for recommendation");
                }
                recommendedItemMap.put(entry.getKey(), recommendByTopGames(ItemType.valueOf(entry.getKey()), topGames));
            } else { // if has favorites, recommend by recommendByFavoriteHistory
                recommendedItemMap.put(entry.getKey(), recommendByFavoriteHistory(favoriteItemIds, entry.getValue(), ItemType.valueOf(entry.getKey())));
            }
        }
        return recommendedItemMap;
    }

    public Map<String, List<Item>> recommendItemsByDefault() throws RecommendationException {
        // default recommend by top games
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        TwitchClient client = new TwitchClient();
        List<Game> topGames;
        try {
            topGames = client.topGames(DEFAULT_GAME_LIMIT);
        } catch (TwitchException e) {
            throw new RecommendationException("Failed to get game data for recommendation");
        }

        for (ItemType type : ItemType.values()) {
            recommendedItemMap.put(type.toString(), recommendByTopGames(type, topGames));
        }
        return recommendedItemMap;
    }


}