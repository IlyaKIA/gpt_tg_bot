package com.example.gpt.service;

import com.example.gpt.source.Room;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AnswerService {
    @Autowired
    RoomService rooms;

    public SendMessage createSimpleMsg(Update update, String msg) {
        return createSimpleMsg(update.getMessage().getChatId().toString(), msg);
    }

    public SendMessage createSimpleMsg(String chatId, String msg) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(String.format(msg));
        return message;
    }

    public SendMessage createGreetingMsg(Update update, String greetingText) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(String.format(greetingText, update.getMessage().getChat().getFirstName()));
        return message;
    }

    public SendMessage gptCompletion(List<CompletionChoice> choices, Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        String text = choices.stream().map(choice -> choice.getText() + "\n").collect(Collectors.joining());
        message.setText(text);
        return message;
    }
    public SendMessage gptChatCompletion(List<ChatCompletionChoice> choices, Long chatId) {
        ChatMessage answer = choices.get(0).getMessage();
        Room room = rooms.get(chatId);
        room.addMsgToChat(answer);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(answer.getContent());
        return sendMessage;
    }

    public SendPhoto dallePicURL(Image image, Long chatId) throws Exception {
        SendPhoto message = new SendPhoto();
        message.setChatId(chatId.toString());
        Pattern pattern = Pattern.compile(".*(img-.*\\.png).*");
        Matcher matcher = pattern.matcher(image.getUrl());
        if (!matcher.matches()) throw new RuntimeException("Didn't found file name in URL");
        InputFile photo = new InputFile(new URI(image.getUrl()).toURL().openStream(), matcher.group(1));
        message.setPhoto(photo);
        return message;
    }

    public SendMessage getErrorText(String errText, Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(errText);
        return message;
    }

    public SendVideo didVideoUrl(String result_url, Long chatId) throws IOException {
        SendVideo message = new SendVideo();
        message.setChatId(chatId.toString());
        Pattern pattern = Pattern.compile(".*/([0-9].*\\.mp4).*");
        Matcher matcher = pattern.matcher(result_url);
        if (!matcher.matches()) throw new RuntimeException("Didn't found file name in URL");
        InputFile video = new InputFile(new URL(result_url).openStream(), matcher.group(1));
        message.setVideo(video);
        return message;
    }
}
