package com.example.onto_1921;

public class DepartureInfo {
    private String protectName;
    private String placeName;
    private String date;
    private String time;
    private String departureArrival;

    public DepartureInfo(String protectName, String placeName, String date, String time, String departureArrival) {
        this.protectName = protectName;
        this.placeName = placeName;
        this.date = date;
        this.time = time;
        this.departureArrival = departureArrival;
    }

    public String getInfo() {
        return protectName + " - " + placeName + " - " + date + " " + time + " - " + departureArrival;
    }

    public String getProtectName() {
        return protectName;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDepartureArrival() {
        return departureArrival;
    }
}
