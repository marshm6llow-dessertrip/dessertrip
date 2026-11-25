package com.example.dessertrip.ui.collect.data;

public class PlaceDto {
    public String id = "";
    public String name = "";
    public String address = "";
    public Double latitude;
    public Double longitude;

    public PlaceDto() { }

    public PlaceDto(String id, String name) { this.id = id; this.name = name; }
}
