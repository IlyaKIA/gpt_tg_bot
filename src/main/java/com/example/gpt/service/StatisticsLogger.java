package com.example.gpt.service;

import com.example.gpt.entity.ChatInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@EnableScheduling
@Component
public class StatisticsLogger {
    private final Set<Long> totalUserCounter = new HashSet<>();
    private final Set<Long> dailyUserCounter = new HashSet<>();
    private int totalRequestCounter = 0;
    private int dailyErrorCounter = 0;
    private int dailyRequestCounter = 0;
    private final Map<Long, ChatInfo> chats = new HashMap<>();
    Date startDate = new Date();

    @Scheduled(cron = "0 0 0 * * *")
    public void StatisticsLogger() {
        log.info(getStatistics());
        log.info(getChatActivityStatistics());
        dailyUserCounter.clear();
        dailyRequestCounter = 0;
        dailyErrorCounter = 0;
        chats.forEach((id, chatInfo) -> chatInfo.clearDailyCounter());
    }

    public void countRequest(org.telegram.telegrambots.meta.api.objects.Chat chat) {
        dailyRequestCounter++;
        totalRequestCounter++;
        dailyUserCounter.add(chat.getId());
        totalUserCounter.add(chat.getId());
        if (chats.containsKey(chat.getId())) {
            chats.get(chat.getId()).countRequest();
        } else {
            chats.put(chat.getId(), new ChatInfo(chat.getId(), chat.getUserName()));
        }
    }

    public void countError() {
        dailyErrorCounter++;
    }

    public String getStatistics() {
        StringBuilder statistic = new StringBuilder();
        statistic.append("%n").append("Daily number of users: ").append(dailyUserCounter.size()).append("%n");
        statistic.append("Number of requests this day: ").append(dailyRequestCounter).append("%n");
        statistic.append("Errors numbers this day: ").append(dailyErrorCounter).append("%n");
        statistic.append("Started: ").append(startDate).append("%n");
        statistic.append("Total number of users: ").append(totalUserCounter.size()).append("%n");
        statistic.append("Total number of requests: ").append(totalRequestCounter);
        return String.format(statistic.toString());
    }

    public String getChatActivityStatistics() {
        StringBuilder statistic = new StringBuilder();
        statistic.append("%n").append("Activity in chats this day:%n");
        chats.forEach((id, chatInfo) -> statistic.append(chatInfo));
        return String.format(statistic.toString());
    }

    public boolean isChatActive(Long chatId) {
        if (chats.containsKey(chatId)) {
            return chats.get(chatId).isActive();
        }
        return true;
    }
}


