package com.example.mapboxtest;

/** Ben - 17/02/20
 * Data Class used to store marker location, userid, typeofbin in JSON format for Realtime Database
 */

public class Data {

    private Double latitude;
    private Double longitude;
    private String userId;
    private String typeOfBin;


    public Data() {
    }

    public Data(Double latitude, Double longitude, String userId, String typeOfBin) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
        this.typeOfBin = typeOfBin;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTypeOfBin() {
        return typeOfBin;
    }

    public void setTypeOfBin(String typeOfBin) {
        this.typeOfBin = typeOfBin;
    }



}
