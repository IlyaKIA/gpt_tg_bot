package com.example.gpt.service.gpt;

public interface GPT_Service {
    Object ask (String text, Long chatId, String userName) throws Exception;
}
