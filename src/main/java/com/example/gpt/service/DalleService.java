package com.example.gpt.service;

import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

@Service
public class DalleService {

    @Autowired
    AnswerService answerService;

    String token = System.getenv("OPENAI_TOKEN");
    OpenAiService service = new OpenAiService(token);

    public SendPhoto ask(String text, Long chatId, String userName) throws Exception {
        if (StringUtils.isEmpty(text)) throw new RuntimeException("I need a text to generate picture");
        CreateImageRequest request = CreateImageRequest.builder()
                .n(1)
                .user(userName)
                .prompt(text)
                .build();
        Image image = service.createImage(request).getData().get(0);
        return answerService.dallePicURL(image, chatId);
    }
}
