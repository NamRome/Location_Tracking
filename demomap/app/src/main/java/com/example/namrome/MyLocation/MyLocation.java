package com.example.namrome.MyLocation;

/**
 * Created by Nam Rome on 2/17/2017.
 */

public class MyLocation {
        private Double lat;
        private Double log;
        private String Times = "";

    public MyLocation(Double lat, Double log, String times) {
        this.lat = lat;
        this.log = log;
        Times = times;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLog() {
        return log;
    }

    public void setLog(Double log) {
        this.log = log;
    }

    public String getTimes() {
        return Times;
    }

    public void setTimes(String times) {
        Times = times;
    }

    public MyLocation() {
        }

    }


