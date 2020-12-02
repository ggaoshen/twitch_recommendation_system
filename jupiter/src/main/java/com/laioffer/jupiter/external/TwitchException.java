package com.laioffer.jupiter.external;

public class TwitchException extends RuntimeException {
    // TwitchException什么时候抛出？
    //         4 1              3 2
    // SERVLET <-> TwitchClient <-> TwitchException
    // 2，3： http error，json format error， twitch api error， etc. 都可以放到TwitchException里
    // 4： TwitchClient handle不了的抛回给servlet的exception也可以放到twitch exception里

    public TwitchException(String errorMessage) {
        super(errorMessage); // super调用父类的构造函数
    }
}
