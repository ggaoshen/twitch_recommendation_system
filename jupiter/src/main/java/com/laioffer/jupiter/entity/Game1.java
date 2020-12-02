package com.laioffer.jupiter.entity;

public class Game1 {
    private final String name;
    private final String developer;
    private final float price;
    private final String releaseDate;

    private Game1(GameBuilder builder) { // constructor - use builder pattern
        // accessor写成private更好，为什么？
        // public的话可以直接initialize:
        // Game game = builder.build(); 和
        // Game game = new Game(builder) 都可以，但是功能一样，没必要
        this.name = builder.name;
        this.developer = builder.developer;
        this.price = builder.price;
        this.releaseDate = builder.releaseDate;

    }

//    public Game(String name, String developer) { // constructor
//        this.name = name;
//        this.developer = developer;
//    }

    public String getName() {
        return name;
    }

//    public Game setName(String name) {
//        if (name == "vincent") {
//            System.out.println(name);
//            return this;
//        }
//        this.name = name;
//        return this;
//    }

    public String getDeveloper() {
        return developer;
    }

//    public Game setDeveloper(String developer) {
//        this.developer = developer;
//        return this;
//    }

    public float getPrice() {
        return price;
    }

//    public Game setPrice(float price) {
//        this.price = price;
//        return this;
//    }

    // builder pattern
    public static class GameBuilder{
        // 为什么需要static？
        // call的时候： Game.GameBuilder builder = new Game.GameBuilder();
        // builder得先于Game initialize，不是static的话需要先new Game
        private String name;
        private String developer;
        private float price;
        private String releaseDate;

        public void setName(String name) {
            this.name = name;
        }

        public void setDeveloper(String developer) {
            this.developer = developer;
        }

        public void setPrice(float price) {
            this.price = price;
        }

        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }

        public Game1 build() {
            return new Game1(this); //
        }
    }

}

// create game object
// Game game = new Game();

// game.setName("vincent"); game.getDeveloper("laioffer"); game.setPrice(10)

// game.name = "vincent";
// vs
// game.setName("vincent"); // 这个可以自己控制logic，see setName

// private variable，没有setter和final有啥区别？private可以class内部改，final内部也改不了

// CONSTRUCTOR如果参数很多，容易initialize错
// 比如 Game game = new Game("vincent", "laioffer", "2020-11-28", 10)
// vs  Game game = new Game("laioffer", "vincent", "2020-11-28", 10)
// 或 Game game = new Game("vincent", "laioffer"); Game game = new Game("vincent", "2020-11-28", 10) 很多中排列组合
// 可以用builder pattern
// GameBuilder builder = new GameBuilder();
// builder.setName("vincent")
// builder.setDeveloper("laioffer")
// Game game = builder.build(); // 想set那个就可以set哪个，不会set错field

// 有什么区别？
// Game.GameBuilder builder = new Game.GameBuilder();
// builder.setName("vincent")
// builder.setDeveloper("laioffer")
// Game game = builder.build();
// vs
// Game game = new Game();
// game.setName("vincent");
// Game里field可以都搞成final，name如果是final，game.setName就不能存在
// 可以初始化的时候设定默认值