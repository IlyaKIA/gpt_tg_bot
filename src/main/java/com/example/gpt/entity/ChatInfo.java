package com.example.gpt.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ChatInfo {
    private long chatId;
    private String userName;
    private int dailyLimit = 15;
    private final Map<Long, Integer> chatIdWithSpecialLimits = new HashMap<>();
    private int dailyCounter = 1;
    private int totalCounter = 1;

    public ChatInfo(long chatId, String userName) {
        this.chatId = chatId;
        this.userName = userName;

        chatIdWithSpecialLimits.put(580704337L, 100);
        if (chatIdWithSpecialLimits.containsKey(chatId)) {
            dailyLimit = chatIdWithSpecialLimits.get(chatId);
        }
    }

    public void countRequest() {
        dailyCounter++;
        totalCounter++;
    }

    public void clearDailyCounter() {
        dailyCounter = 0;
    }

    public boolean isActive() {
        return dailyLimit > dailyCounter;
    }

    @Override
    public String toString() {
        return "Id " + chatId +
                ", UserName '" + userName + '\'' +
                ", DailyLimit " + dailyLimit +
                ", Requests this day " + dailyCounter +
                ", Total Requests " + totalCounter + "\n";
    }
}
