package com.example.gpt.service;

import com.example.gpt.source.Room;

import java.util.HashMap;

public class RoomService extends HashMap<Long, Room> {
    private RoomService () {}
    private static volatile RoomService instance = null;

    public static RoomService getInstance() {
        if (instance == null) {
            synchronized (RoomService.class) {
                if (instance == null) {
                    instance = new RoomService();
                }
            }
        }
        return instance;
    }
}
