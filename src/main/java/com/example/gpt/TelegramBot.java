package com.example.gpt;

import com.example.gpt.config.BotConfig;
import com.example.gpt.service.AnswerService;
import com.example.gpt.service.gpt.GptChatService;
import com.example.gpt.service.gpt.GptCompletionService;
import com.example.gpt.service.gpt.DalleService;
import com.example.gpt.source.Room;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.gpt.source.Commands.*;
import static com.example.gpt.source.MessageTexts.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    private final Map<Long, Room> rooms = new HashMap<>(); // TODO move to singleton

    @Autowired
    AnswerService answerService;
    @Autowired
    GptCompletionService completionService;
    @Autowired
    GptChatService chatService;
    @Autowired
    DalleService dalleService;
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    public TelegramBot (BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand(COMMAND_START, "main menu"));
        listOfCommands.add(new BotCommand(COMMAND_HI_GPT, "start chatting with ChatGPT 3.5"));
        listOfCommands.add(new BotCommand(COMMAND_ASK_GPT, "ask question ChatGPT 3"));
        listOfCommands.add(new BotCommand(COMMAND_NEW_PIC, "create a picture by description. Using DALL-E AI"));
        listOfCommands.add(new BotCommand(COMMAND_ABOUT, "about author"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        executorService.execute(() -> {
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
                            message = answerService.chatGreeting(update);
                            rooms.put(chatId, new Room(COMMAND_HI_GPT));
                        }
                        case COMMAND_ASK_GPT -> {
                            message = answerService.completionGreeting(update);
                            rooms.put(chatId, new Room(COMMAND_ASK_GPT));
                        }
                        case COMMAND_NEW_PIC -> {
                            message = answerService.picGreeting(update);
                            rooms.put(chatId, new Room(COMMAND_NEW_PIC));
                        }
                        default -> {
                            if (rooms.containsKey(chatId)) {
                                sendWaitingMsg(chatId);
                                message = chooseService(rooms.get(chatId).getCurrentRoom(), update);
                            } else {
                                message = answerService.createSimpleMsg(update, INTRODUCTION);
                            }
                        }
                    }

                    if (rooms.containsKey(chatId) && rooms.get(chatId).isTempMsgExist()) {
                        DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), rooms.get(chatId).getTempMsgIdAndClear());
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
        });
    }

    private void sendWaitingMsg(Long chatId) throws TelegramApiException {
        Message execute = execute(answerService.createSimpleMsg(chatId.toString(), WAITING_MSG));
        rooms.get(chatId).setTempMsgId(execute.getMessageId());
    }

    private Object chooseService(String room, Update update) {
        try {
            switch (room) {
                case COMMAND_HI_GPT -> {
                    return chatService.ask(update.getMessage().getText(),
                            update.getMessage().getChatId(), update.getMessage().getChat().getUserName());
                }
                case COMMAND_ASK_GPT -> {
                    return completionService.ask(update.getMessage().getText(),
                            update.getMessage().getChatId(), update.getMessage().getChat().getUserName());
                }
                case COMMAND_NEW_PIC -> {
                    return dalleService.ask(update.getMessage().getText(),
                            update.getMessage().getChatId(), update.getMessage().getChat().getUserName());
                }
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

