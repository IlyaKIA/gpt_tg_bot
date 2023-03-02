package com.example.gpt.service;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.image.Image;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.gpt.source.MessageTexts.*;

@Service
public class AnswerService {

    public SendMessage createIntroduction(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(String.format(INTRODUCTION));
        return message;
    }

    public SendMessage gptGreeting(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(String.format(START_CONVERSATION, update.getMessage().getChat().getFirstName()));
        return message;
    }

    public SendMessage picGreeting(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(String.format(START_DRAWING, update.getMessage().getChat().getFirstName()));
        return message;
    }

    public SendMessage gptCompletion(List<CompletionChoice> choices, Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        String text = choices.stream().map(choice -> choice.getText() + "\n").collect(Collectors.joining());
        message.setText(text);
        return message;
    }

    public SendMessage dallePicURL(List<Image> images, Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        String text = images.stream().map(image -> image.getUrl() + "\n").collect(Collectors.joining());
        message.setText(text);
        return message;
    }

    public SendMessage getErrorText(String errText, Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(errText);
        return message;
    }
}
