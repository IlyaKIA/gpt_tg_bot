package com.example.gpt.service.gpt;

import com.example.gpt.service.AnswerService;
import com.example.gpt.service.RoomService;
import com.example.gpt.source.Room;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

@Service
public class GptChatService implements GPT_Service {

    @Autowired
    AnswerService answerService;
    RoomService rooms = RoomService.getInstance();
    String token = System.getenv("OPENAI_TOKEN");
    OpenAiService service = new OpenAiService(token, Duration.ofSeconds(60L));

    @Override
    public SendMessage ask(String text, Long chatId, String userName) throws OpenAiHttpException {
        if (StringUtils.isEmpty(text)) throw new RuntimeException("Send me a text if you have a question");
        Room room = rooms.get(chatId);
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.USER.value(), text);
        room.getMessages().add(systemMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .user(userName)
                .messages(room.getMessages())
                .n(1)
                .maxTokens(2048)
                .logitBias(new HashMap<>())
                .build();

        List<ChatCompletionChoice> choices = service.createChatCompletion(chatCompletionRequest).getChoices();

        return answerService.gptChatCompletion(choices, chatId);
    }
}
