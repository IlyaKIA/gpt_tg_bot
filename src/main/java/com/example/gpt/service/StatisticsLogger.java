package com.example.gpt.service;

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
    Date startDate = new Date();

    @Scheduled(cron = "0 0 0 * * *")
    public void StatisticsLogger() {
        log.info(getStatistics());
        dailyUserCounter.clear();
        dailyRequestCounter = 0;
        dailyErrorCounter = 0;
    }

    public void countRequest(Long userId) {
        dailyRequestCounter++;
        totalRequestCounter++;
        dailyUserCounter.add(userId);
        totalUserCounter.add(userId);
    }

    public void countError() {
        dailyErrorCounter++;
    }

    public String getStatistics() {
        StringBuilder statistic = new StringBuilder();
        statistic.append("%n").append("Daily number of users: ").append(dailyUserCounter.size()).append("%n");
        statistic.append("Daily number of requests: ").append(dailyRequestCounter).append("%n");
        statistic.append("Daily errors number: ").append(dailyErrorCounter).append("%n");
        statistic.append("Started: ").append(startDate).append("%n");
        statistic.append("Total number of users: ").append(totalUserCounter.size()).append("%n");
        statistic.append("Total number of requests: ").append(totalRequestCounter);
        return String.format(statistic.toString());
    }
}


