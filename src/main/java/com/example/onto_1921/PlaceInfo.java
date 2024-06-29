package com.example.onto_1921;

class PlaceInfo {
    private double latitude;
    private double longitude;
    private String name;
    private boolean hasArrived; // 도착 여부를 추적하는 필드

    public PlaceInfo(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.hasArrived = false; // 초기 상태는 도착하지 않은 것으로 설정
    }

    // getter 및 setter 메서드 추가
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public boolean hasArrived() {
        return hasArrived;
    }

    public void setHasArrived(boolean hasArrived) {
        this.hasArrived = hasArrived;
    }
}
