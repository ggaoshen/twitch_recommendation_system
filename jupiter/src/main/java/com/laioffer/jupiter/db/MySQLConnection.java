package com.laioffer.jupiter.db;

import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import com.laioffer.jupiter.entity.User;
import com.mysql.jdbc.Driver;

import java.sql.*;
import java.util.*;

public class MySQLConnection {
    private final Connection conn; // final只有一次初始化的机会

    public MySQLConnection() throws MySQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance(); // JDBC library必须有这句，来处理一些bug
            // new一个Driver instance，和后面没有直接逻辑关系，但是会设置一些参数
            // 上面这是个reflection写法，相当于 com.mysql.cj.jdbc.Driver driver = new Driver();

            conn = DriverManager.getConnection(MySQLDBUtil.getMySQLAddress());
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Failed to connect to Database");
        }
    }

    public void close() {
        // 为什么要close connection？
        // db的connection数量有限，不断开会占资源
        // 开着connection也占一些内存
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setFavoriteItem(String userId, Item item) throws MySQLException {
        if (conn == null) {
            throw new MySQLException("Failed to connect to DB");
        }

        saveItem(item);

        // Insert userid and itemid favorite table, may need to insert item table (if item doesn't already exist in item table)
        // METHOD 1: String sql = String.format("INSERT INTO favorite_records (user_id, item_id) VALUES (%s, %s)", userId, item.getId());
        // METHOD 2: 用sql native的？，？。比如%s不能控制加入什么，用？可以用java.SQL库的一些保护
        // e.g.1: String template = "INSERT INTO favorite_records (user_id, item_id) VALUES ('1111; DROP TABLES;')"; 删库跑路
        // e.g.2: SELECT * FROM users WHERE user_id = "1111 or 1=1"; 返回所有user_id
        String sql = "INSERT IGNORE INTO favorite_records (user_id, item_id) VALUES (?, ?)";
        // IGNORE keyword：如果加入了duplicate，就直接ignore了
        PreparedStatement statement = null; // 用PreparedStatement写安全性最高
        try {
            statement = conn.prepareStatement(sql);
            statement.setString(1, userId); // 注意不是从0开始，而是从1开始
            statement.setString(2, item.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new MySQLException("Failed to connect to DB");
        }
    }

    public void unSetFavoriteItem(String userId, Item item) throws MySQLException {
        if (conn == null) {
            throw new MySQLException("Failed to connect to DB");
        }

        String sql = "DELETE FROM favorite_records WHERE user_id = ? AND item_id = ?";
        // IGNORE keyword：如果加入了duplicate，就直接ignore了
        PreparedStatement statement = null; // 用PreparedStatement写安全性最高
        try {
            statement = conn.prepareStatement(sql);
            statement.setString(1, userId); // 注意不是从0开始，而是从1开始
            statement.setString(2, item.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new MySQLException("Failed to connect to DB");
        }
    }

    public void saveItem(Item item)  throws MySQLException {
        if (conn == null) {
            throw new MySQLException("Failed to connect to DB");
        }
        String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = null; // 这样写安全性最高
        try {
            statement = conn.prepareStatement(sql);
            statement.setString(1, item.getId()); // 注意不是从0开始，而是从1开始
            statement.setString(2, item.getTitle());
            statement.setString(3, item.getUrl());
            statement.setString(4, item.getThumbnailUrl());
            statement.setString(5, item.getBroadcasterName());
            statement.setString(6, item.getGameId());
            statement.setString(7, item.getType().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new MySQLException("Failed to connect to DB");
        }
    }

    // return userId only, used for recommendation (推荐结果里不包括已经收藏过的items)
    public Set<String> getFavoriteItemIds (String userId) throws MySQLException {

        if (conn == null) {
            throw new MySQLException("Failed to connect to DB");
        }

        Set<String> favoriteItemIds = new HashSet<>();
        String sql = "SELECT item_id FROM favorite_records WHERE user_id = ?";
        PreparedStatement statement = null; // 用PreparedStatement写安全性最高
        try {
            statement = conn.prepareStatement(sql);
            statement.setString(1, userId); // 注意不是从0开始，而是从1开始
            ResultSet rs = statement.executeQuery(); // executeQuery有返回值，executeUpdate没有返回值
            // 读取返回值：java object一般都有iterator，这里ResultSet需要iterate rows
            // 如果一个table有100个column，可以用ORM，map table to java object

            while (rs.next()) { // rs.next()是二合一操作=rs.next() & rs.exist()。默认指向-1的位置，上来.NEXT就是对的
                String itemId = rs.getString("item_id"); // 读取多列数据就.getString(col_i)
                favoriteItemIds.add(itemId);
            }
        } catch (SQLException e) {
            throw new MySQLException("Failed to connect to DB");
        }
        return favoriteItemIds;
    }

    public Map<String, List<Item>> getFavoriteItems (String userId) throws MySQLException {
        // return all properties of Item

        if (conn == null) {
            throw new MySQLException("Failed to connect to DB");
        }

        // 初始化 return map
        Map<String, List<Item>> itemMap = new HashMap<>(); // map<ItemType enum, list of items>
        for (ItemType type: ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }

//        // METHOD 1: JOIN
//        String sql = "SELECT DISTINCT items.* FROM items LEFT JOIN favorite_records " +
//                "ON items.id = favorite_records.item_id WHERE user_id = ?";

        // METHOD 2: 分别call
        Set<String> favoriteItemIds = getFavoriteItemIds(userId);
        String sql = "SELECT * FROM items WHERE id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId); // 注意不是从0开始，而是从1开始
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    ItemType itemType = ItemType.valueOf(rs.getString("type"));
                    Item item = new Item.Builder()
                            .id(rs.getString("id"))
                            .title(rs.getString("title"))
                            .url(rs.getString("url"))
                            .thumbnailUrl(rs.getString("thumbnail_url"))
                            .broadcasterName(rs.getString("broadcaster_name"))
                            .gameId(rs.getString("game_id"))
                            .type(itemType)
                            .build();
                    itemMap.get(rs.getString("type")).add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to connect to DB");
        }
        return itemMap;
    }

    public Map<String, List<String>> getFavoriteGameIds(Set<String> favoriteItemIds) throws MySQLException {
        // get all properties of games

        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        Map<String, List<String>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }
        String sql = "SELECT game_id, type FROM items WHERE id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    itemMap.get(rs.getString("type")).add(rs.getString("game_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get favorite game ids from Database");
        }
        return itemMap;
    }


    // AUTHORIZATION
    public String verifyLogin(String userId, String password) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to DB");
        }

        String name = "";
        String sql = "SELECT first_name, last_name FROM users WHERE id = ? AND password = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                name = rs.getString("first_name") + " " + rs.getString("last_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to verify user id and password from Database");
        }
        return name;
    }

    public boolean addUser (User user) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to DB");
        }

        String sql = "INSERT IGNORE INTO users VALUES(?,?,?,?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, user.getUserId());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());

            return statement.executeUpdate()==1; // insert了几行就return几，这里我们只insert一行，成功就是1

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to verify user id and password from Database");
        }
    }

}
