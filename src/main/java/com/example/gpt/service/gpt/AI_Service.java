package com.example.gpt.service.gpt;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface AI_Service {
    Object ask (Update update, DefaultAbsSender sender) throws Exception;
    String getClassName();
}
