package com.example.gpt.service;

import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import java.util.List;

@Service
public class DalleService {

    @Autowired
    AnswerService answerService;

    String token = System.getenv("OPENAI_TOKEN");
    OpenAiService service = new OpenAiService(token);

    public SendPhoto ask(String text, Long chatId) throws Exception {
        CreateImageRequest request = CreateImageRequest.builder()
                .prompt(text)
                .build();
        List<Image> images = service.createImage(request).getData();
        return answerService.dallePicURL(images, chatId);
    }
}
