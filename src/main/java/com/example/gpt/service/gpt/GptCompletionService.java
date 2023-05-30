package com.example.gpt.service.gpt;

import com.example.gpt.service.AnswerService;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

@Service
public class GptCompletionService implements AI_Service {

    @Autowired
    AnswerService answerService;
    String token = System.getenv("OPENAI_TOKEN");
    OpenAiService service = new OpenAiService(token, Duration.ofSeconds(60L));

    @Override
    public SendMessage ask(Update update, DefaultAbsSender sender) throws OpenAiHttpException {
        String text = update.getMessage().getText();
        String userName = update.getMessage().getChat().getUserName();
        Long chatId = update.getMessage().getChatId();
        if (StringUtils.isEmpty(text)) throw new RuntimeException("Send me text if you have a question");
        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("text-davinci-003")
                .prompt(text)
                .n(1)
                .maxTokens(1024)
                .user(userName)
                .logitBias(new HashMap<>())
                .build();

        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();

        return answerService.gptCompletion(choices, chatId);
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }
}
