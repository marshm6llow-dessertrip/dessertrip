package com.example.dessertrip.ui.friend;

public class DessertSticker {

    private final int imageResId;
    private final String name;
    private final boolean gifted;

    public DessertSticker(int imageResId, String name, boolean gifted) {
        this.imageResId = imageResId;
        this.name = name;
        this.gifted = gifted;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getName() {
        return name;
    }

    public boolean isGifted() {
        return gifted;
    }
}