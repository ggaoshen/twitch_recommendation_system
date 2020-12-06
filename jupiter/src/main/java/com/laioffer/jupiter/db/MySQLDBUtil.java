package com.laioffer.jupiter.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MySQLDBUtil {
    private static final String INSTANCE = "twitchdb.cxyvqpvyz2oj.us-west-2.rds.amazonaws.com"; // get from RDS: Endpoint属性
    private static final String PORT_NUM = "3306";
    private static final String DB_NAME = "raw_twitch";

    public static String getMySQLAddress() throws IOException {
        Properties prop = new Properties();
        String propFileName = "config.properties";

        InputStream inputStream = MySQLDBUtil.class.getClassLoader().getResourceAsStream(propFileName); //直接从resources里读file
        // 为什么读成一个stream？因为不确定文件有多长，可能很长，不能一次都读入内存
        prop.load(inputStream); // Properties是一个hashmap<value=username/pw， value=具体值>

        String username = prop.getProperty("user");
        String password = prop.getProperty("password");

        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true&serverTimezone=UTC&createDatabaseIfNotExist=true",
                INSTANCE, PORT_NUM, DB_NAME, username, password); // DB_NAME不写也行，但是操作db得加use raw_twitch; select*from....

    }


}
