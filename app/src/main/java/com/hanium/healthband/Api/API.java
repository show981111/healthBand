package com.hanium.healthband.Api;

public class API {
    public static final String BASE_URL = "";

    public static final String GetSensor = "http://ec2-3-34-84-225.ap-northeast-2.compute.amazonaws.com:8000/sensorData/get/";
    public static final String HEART = "http://ec2-3-34-84-225.ap-northeast-2.compute.amazonaws.com:8000/sensorData/heartRate/";
    public static final String SOUND = "http://ec2-3-34-84-225.ap-northeast-2.compute.amazonaws.com:8000/sensorData/sound/";
    public static final String postSensor = "http://ec2-3-34-84-225.ap-northeast-2.compute.amazonaws.com:8000/wearerData/post/";
    public static final String postLink = "http://ec2-3-34-84-225.ap-northeast-2.compute.amazonaws.com:8000/linkedUser/post/";
    public static final String postEvent = "http://ec2-3-34-84-225.ap-northeast-2.compute.amazonaws.com:8000/wearerEvent/post/";
    public static final String postFCM = "http://ec2-3-34-84-225.ap-northeast-2.compute.amazonaws.com:8000/user/changeFcmToken/";
    public static final String postLocation = "http://ec2-3-34-84-225.ap-northeast-2.compute.amazonaws.com:8000/wearerLocation/post/";
    public static final String postMeter = "";
}
