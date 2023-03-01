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
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;

import static com.example.gpt.source.Commands.*;

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
            SendMessage message; // Create a SendMessage object with mandatory fields
            switch (receivedText.toLowerCase()) {
                case COMMAND_START: {
                    message = answerService.createIntroduction(update);
                    break;
                }
                case HI_GPT: {
                    message = answerService.gptGreeting(update);
                    rooms.put(chatId, HI_GPT);
                    break;
                }
                case NEW_PIC: {
                    message = answerService.picGreeting(update);
                    rooms.put(chatId, NEW_PIC);
                    break;
                }
                default: {
                    if(rooms.containsKey(chatId)) {
                        message = chooseService(rooms.get(chatId), update);
                    } else {
                        message = answerService.createIntroduction(update);
                    }
                }
            }

            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                log.error("onUpdateReceived", e);
            }
        }
    }

    private SendMessage chooseService(String room, Update update) {
        if (HI_GPT.equals(room)) {
            return chatGPTService.ask(update.getMessage().getText(), update.getMessage().getChatId());
        } else if (NEW_PIC.equals(room)) {
            return dalleService.ask(update.getMessage().getText(), update.getMessage().getChatId());
        } else {
                //TODO
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

