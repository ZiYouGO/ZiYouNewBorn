package com.mingle.ZiYou.util;

/**
 * Created by maxiangyu on 2016/6/13.
 */
public class DistanceCalculator {

    private static double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    //距离单位：千米
    public static double getDistance(String lat1Str, String lng1Str, double lat2Str, double lng2Str) {
        Double lat1 = Double.parseDouble(lat1Str);
        Double lng1 = Double.parseDouble(lng1Str);
        Double lat2 = lat2Str;
        Double lng2 = lng2Str;

        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double difference = radLat1 - radLat2;
        double mdifference = rad(lng1) - rad(lng2);
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(difference / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(mdifference / 2), 2)));
        distance = distance * EARTH_RADIUS;
        //distance = Math.round(distance * 10000) / 10000;
//        String distanceStr = distance+"";
//        distanceStr = distanceStr.
//                substring(0, distanceStr.indexOf("."));

        return distance;
    }

    public static void main(String[] args) {
        //济南国际会展中心经纬度：117.11811  36.68484
        //趵突泉：117.00999000000002  36.66123
        //System.out.println(getDistance("117.11811","36.68484","117.00999000000002","36.66123"));

    }
}
