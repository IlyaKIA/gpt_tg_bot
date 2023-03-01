package com.example.gpt.service;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.HashMap;
import java.util.List;

@Service
public class DalleService {

    @Autowired
    AnswerService answerService;
    @Value("${openAi.token}")
    String token;


    public SendMessage ask(String text, Long chatId) {
        OpenAiService service = new OpenAiService(token);
        CreateImageRequest request = CreateImageRequest.builder()
                .prompt(text)
                .build();
        List<Image> images = service.createImage(request).getData();
        return answerService.dallePicURL(images, chatId);
    }
}
