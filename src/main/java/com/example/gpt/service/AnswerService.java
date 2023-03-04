package com.example.gpt.service;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.image.Image;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public SendPhoto dallePicURL(List<Image> images, Long chatId) throws Exception {
        SendPhoto message = new SendPhoto();
        message.setChatId(chatId.toString());
        Pattern pattern = Pattern.compile(".*(img-.*\\.png).*");
        Matcher matcher = pattern.matcher(images.get(0).getUrl());
        if (!matcher.matches()) throw new RuntimeException("Didn't found file name in URL");
        InputFile photo = new InputFile(new URI(images.get(0).getUrl()).toURL().openStream(), matcher.group(1));
        message.setPhoto(photo);
        return message;
    }

    public SendMessage getErrorText(String errText, Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(errText);
        return message;
    }
}
