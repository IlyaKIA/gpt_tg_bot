package com.example.gpt;

import com.example.gpt.config.BotConfig;
import com.example.gpt.service.AnswerService;
import com.example.gpt.service.ChatGPT_Service;
import com.example.gpt.service.DalleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;

import static com.example.gpt.source.Commands.*;
import static com.example.gpt.source.MessageTexts.ERROR_MSG;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    private final HashMap<Long, String> rooms = new HashMap<>();

    @Autowired
    AnswerService answerService;
    @Autowired
    ChatGPT_Service chatGPTService;
    @Autowired
    DalleService dalleService;

    public TelegramBot (BotConfig config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String receivedText = update.getMessage().getText();
            Object message;
            switch (receivedText.toLowerCase()) {
                case COMMAND_START -> {
                    message = answerService.createIntroduction(update);
                }
                case HI_GPT -> {
                    message = answerService.gptGreeting(update);
                    rooms.put(chatId, HI_GPT);
                }
                case NEW_PIC -> {
                    message = answerService.picGreeting(update);
                    rooms.put(chatId, NEW_PIC);
                }
                default -> {
                    if (rooms.containsKey(chatId)) {
                        message = chooseService(rooms.get(chatId), update);
                    } else {
                        message = answerService.createIntroduction(update);
                    }
                }
            }

            try {
                if (message instanceof SendMessage) {
                    execute((SendMessage) message);
                } else if (message instanceof SendPhoto) {
                    execute((SendPhoto) message);
                }
            } catch (TelegramApiException e) {
                log.error("onUpdateReceived", e);
            }
        }
    }

    private Object chooseService(String room, Update update) {
        try {
            if (HI_GPT.equals(room)) {
                return chatGPTService.ask(update.getMessage().getText(), update.getMessage().getChatId());
            } else if (NEW_PIC.equals(room)) {
                return dalleService.ask(update.getMessage().getText(), update.getMessage().getChatId());
            } else {
                //TODO
            }
        } catch (Exception e) {
            log.error("Problems with GPT connection", e);
            return answerService.getErrorText(String.format(ERROR_MSG, e.getMessage()), update.getMessage().getChatId());
        }
        return null;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}

