package com.example.gpt;

import com.example.gpt.config.BotConfig;
import com.example.gpt.service.AnswerService;
import com.example.gpt.service.ChatGPT_Service;
import com.example.gpt.service.DalleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.gpt.source.Commands.*;
import static com.example.gpt.source.MessageTexts.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    private final HashMap<Long, String> rooms = new HashMap<>();
    private final Map<Long, Integer> CHAT_WAITING_ANSWER = new HashMap<>();

    @Autowired
    AnswerService answerService;
    @Autowired
    ChatGPT_Service chatGPTService;
    @Autowired
    DalleService dalleService;

    public TelegramBot (BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand(COMMAND_START, "main menu"));
        listofCommands.add(new BotCommand(COMMAND_HI_GPT, "start conversation with ChatGPT"));
        listofCommands.add(new BotCommand(COMMAND_NEW_PIC, "create a picture by description"));
        listofCommands.add(new BotCommand(COMMAND_ABOUT, "about author"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String receivedText = update.getMessage().getText();
            Object message;
            try {
            switch (receivedText.toLowerCase()) {
                case COMMAND_START -> {
                    message = answerService.createSimpleMsg(update, INTRODUCTION);
                    rooms.remove(chatId);
                }
                case COMMAND_ABOUT -> {
                    message = answerService.createSimpleMsg(update, ABOUT);
                    rooms.remove(chatId);
                }
                case COMMAND_HI_GPT -> {
                    message = answerService.gptGreeting(update);
                    rooms.put(chatId, COMMAND_HI_GPT);
                }
                case COMMAND_NEW_PIC -> {
                    message = answerService.picGreeting(update);
                    rooms.put(chatId, COMMAND_NEW_PIC);
                }
                default -> {
                    if (rooms.containsKey(chatId)) {
                        sendWaitingMsg(chatId);
                        message = chooseService(rooms.get(chatId), update);
                    } else {
                        message = answerService.createSimpleMsg(update, INTRODUCTION);
                    }
                }
            }

            if (CHAT_WAITING_ANSWER.containsKey(chatId)) {
                DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), CHAT_WAITING_ANSWER.get(chatId));
                CHAT_WAITING_ANSWER.remove(chatId);
                execute(deleteMessage);
            }
            if (message instanceof SendMessage) {
                execute((SendMessage) message);
            } else if (message instanceof SendPhoto) {
                execute((SendPhoto) message);
            }
            } catch (TelegramApiException e) {
                log.error("Problem with sending a message", e);
            }
        }
    }

    private void sendWaitingMsg(Long chatId) throws TelegramApiException {
        Message execute = execute(answerService.createSimpleMsg(chatId.toString(), WAITING_MSG));
        CHAT_WAITING_ANSWER.put(chatId, execute.getMessageId());
    }

    private Object chooseService(String room, Update update) {
        try {
            if (COMMAND_HI_GPT.equals(room)) {
                return chatGPTService.ask(update.getMessage().getText(), update.getMessage().getChatId(), update.getMessage().getChat().getUserName());
            } else if (COMMAND_NEW_PIC.equals(room)) {
                return dalleService.ask(update.getMessage().getText(), update.getMessage().getChatId(), update.getMessage().getChat().getUserName());
            }
        } catch (Exception e) {
            log.error("We have some problems:", e);
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

