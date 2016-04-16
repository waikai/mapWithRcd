package com.example.kwang27.secmap;

/**
 * Created by kwang27 on 4/16/16.
 */
public class LocalInfo {
    Double lat;
    Double lng;
    Integer maxDb;
    Integer lab;
    public LocalInfo(double clat, double clng, int cmax, int clable){
        lat = clat;
        lng = clng;
        maxDb = cmax;
        lab = clable;
    }

}
