package com.example.gpt;

import com.example.gpt.config.BotConfig;
import com.example.gpt.service.AnswerService;
import com.example.gpt.service.RoomService;
import com.example.gpt.service.StatisticsLogger;
import com.example.gpt.service.gpt.*;
import com.example.gpt.source.Room;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.gpt.source.Commands.*;
import static com.example.gpt.source.MessageTexts.*;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

    private final Map<String, AI_Service> servicesByName;
    final BotConfig config;
    RoomService rooms;
    AnswerService answerService;
    GptCompletionService completionService;
    GptChatService chatService;
    DalleService dalleService;
    StatisticsLogger statisticsLogger;
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    @Autowired
    public TelegramBot (BotConfig config, RoomService rooms,
                        AnswerService answerService,
                        GptCompletionService completionService,
                        GptChatService chatService,
                        DalleService dalleService,
                        StatisticsLogger statisticsLogger,
                        List<AI_Service> gptServices) {
        this.config = config;
        this.rooms = rooms;
        this.answerService = answerService;
        this.completionService = completionService;
        this.chatService = chatService;
        this.dalleService = dalleService;
        this.statisticsLogger = statisticsLogger;
        this.servicesByName = gptServices.stream()
                .collect(Collectors.toMap(AI_Service::getClassName, Function.identity()));

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand(COMMAND_START, "main menu"));
        listOfCommands.add(new BotCommand(COMMAND_HI_GPT, "start chatting with ChatGPT 3.5"));
        listOfCommands.add(new BotCommand(COMMAND_ASK_GPT, "ask question ChatGPT 3"));
        listOfCommands.add(new BotCommand(COMMAND_NEW_PIC, "create a picture by description. Using DALL-E AI"));
        listOfCommands.add(new BotCommand(COMMAND_LIVE_AVATAR, "create a animate avatar by photo. Using D-ID"));
        listOfCommands.add(new BotCommand(COMMAND_ABOUT, "about author"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            statisticsLogger.countError();
            log.error("Error setting bot's command list: " + e.getMessage());
        }

    }

    @Override
    public void onUpdateReceived(Update update) {
        executorService.execute(() -> {
            if (update.hasMessage() && (update.getMessage().hasText() || update.getMessage().hasPhoto())) {
                Long chatId = update.getMessage().getChatId();
                String receivedText = update.getMessage().getText();
                if (receivedText != null) receivedText = receivedText.toLowerCase();
                else receivedText = "msgWithPhoto";
                Object message;
                try {
                    switch (receivedText) {
                        case COMMAND_STATISTICS -> {
                            message = answerService.createSimpleMsg(update, statisticsLogger.getStatistics());
                            rooms.remove(chatId);
                        }
                        case COMMAND_CHAT_STATISTICS -> {
                            message = answerService.createSimpleMsg(update, statisticsLogger.getChatActivityStatistics());
                            rooms.remove(chatId);
                        }
                        case COMMAND_START -> {
                            message = answerService.createSimpleMsg(update, INTRODUCTION);
                            rooms.remove(chatId);
                        }
                        case COMMAND_ABOUT -> {
                            message = answerService.createSimpleMsg(update, ABOUT);
                            rooms.remove(chatId);
                        }
                        case COMMAND_HI_GPT -> {
                            message = answerService.createGreetingMsg(update, START_CHATTING);
                            rooms.put(chatId, new Room(GptChatService.class.getName()));
                        }
                        case COMMAND_ASK_GPT -> {
                            message = answerService.createGreetingMsg(update, START_CONVERSATION);
                            rooms.put(chatId, new Room(GptCompletionService.class.getName()));
                        }
                        case COMMAND_NEW_PIC -> {
                            message = answerService.createGreetingMsg(update, START_DRAWING);
                            rooms.put(chatId, new Room(DalleService.class.getName()));
                        }
                        case COMMAND_LIVE_AVATAR -> {
                            message = answerService.createGreetingMsg(update, START_AVATAR);
                            rooms.put(chatId, new Room(DidService.class.getName()));
                        }
                        default -> {
                            if (rooms.containsKey(chatId)) {
                                if (statisticsLogger.isChatActive(chatId)) {
                                    sendWaitingMsg(chatId);
                                    statisticsLogger.countRequest(update.getMessage().getChat());
                                    message = chooseService(rooms.get(chatId).getGptService(), update);
                                } else {
                                    message = answerService.createSimpleMsg(update, LIMIT_MSG);
                                }
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
                    } else if (message instanceof SendVideo) {
                        execute((SendVideo) message);
                    }
                } catch (TelegramApiException e) {
                    statisticsLogger.countError();
                    log.error("Problem with sending a message", e);
                }
            }
        });
    }

    private void sendWaitingMsg(Long chatId) throws TelegramApiException {
        Message execute = execute(answerService.createSimpleMsg(chatId.toString(), WAITING_MSG));
        rooms.get(chatId).setTempMsgId(execute.getMessageId());
    }

    private Object chooseService(String serviceName, Update update) {
        try {
            AI_Service service = servicesByName.get(serviceName);
            return service.ask(update, this);
        } catch (Exception e) {
            statisticsLogger.countError();
            log.error("We have some problems:", e);
            return answerService.getErrorText(String.format(ERROR_MSG, e.getMessage()), update.getMessage().getChatId());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getTg_token();
    }
}
