package com.example.gpt.service.gpt;

import com.example.gpt.service.AnswerService;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Duration;

@Service
public class DalleService implements AI_Service {

    @Autowired
    AnswerService answerService;

    String token = System.getenv("OPENAI_TOKEN");
    OpenAiService service = new OpenAiService(token, Duration.ofSeconds(30L));
    @Override
    public SendPhoto ask(Update update, DefaultAbsSender sender) throws Exception {
        String text = update.getMessage().getText();
        String userName = update.getMessage().getChat().getUserName();
        Long chatId = update.getMessage().getChatId();
        if (StringUtils.isEmpty(text)) throw new RuntimeException("I need a text to generate picture");
        CreateImageRequest request = CreateImageRequest.builder()
                .n(1)
                .user(userName)
                .prompt(text)
                .build();
        Image image = service.createImage(request).getData().get(0);
        return answerService.dallePicURL(image, chatId);
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }
}
