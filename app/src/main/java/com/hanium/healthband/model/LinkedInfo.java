package com.hanium.healthband.model;

import java.util.ArrayList;

public class LinkedInfo {
    String user_type;
    String username;
    ArrayList<User> linkedList = new ArrayList<>();

    public LinkedInfo(String user_type, String username, ArrayList<User> linkedList) {
        this.user_type = user_type;
        this.username = username;
        this.linkedList = linkedList;
    }
}
