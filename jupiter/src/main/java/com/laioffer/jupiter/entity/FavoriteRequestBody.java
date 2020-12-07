package com.laioffer.jupiter.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FavoriteRequestBody {
    private final Item favoriteItem;

    @JsonCreator // 这个是call constructor
    public FavoriteRequestBody(@JsonProperty("favorite") Item favoriteItem) {
        // @JsonProperty("favorite") 只能把JSON convert成java对象，不能反过来
        // 如果放在class name下，也可以，那样不仅可以json->java可以java->json,
        // 但是这里我们不需要把json返回给前端
        this.favoriteItem = favoriteItem;
    }

    public Item getFavoriteItem() {
        return favoriteItem;
    }

}
