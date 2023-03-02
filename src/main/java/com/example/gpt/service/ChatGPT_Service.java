package com.example.gpt.service;

import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

@Service
public class ChatGPT_Service {

    @Autowired
    AnswerService answerService;
    String token = System.getenv("OPENAI_TOKEN");
    OpenAiService service = new OpenAiService(token, Duration.ofSeconds(60L));


    public SendMessage ask(String text, Long chatId) throws OpenAiHttpException {
        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("text-davinci-003")
                .prompt(text)
                .n(1)
                .maxTokens(2000)
                .user("testing")
                .logitBias(new HashMap<>())
                .build();

        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();

        return answerService.gptCompletion(choices, chatId);
    }
}
