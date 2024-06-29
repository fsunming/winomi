package com.example.onto_1921;

import com.google.gson.annotations.SerializedName;

public class UserImage {
    @SerializedName("imageData")
    private String imageData;

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }
}
