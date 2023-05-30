package com.example.gpt.service.gpt;

import com.example.gpt.config.BotConfig;
import com.example.gpt.entity.DidAnimationTaskResultDTO;
import com.example.gpt.entity.DidResultInfoDTO;
import com.example.gpt.service.AnswerService;
import com.example.gpt.service.DIdRequestService;
import com.google.gson.Gson;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Duration;

import static java.lang.Thread.sleep;


@Service
public class DidService implements AI_Service {

    @Autowired
    AnswerService answerService;
    @Autowired
    BotConfig botConfig;
    @Autowired
    DIdRequestService d_idRequestService;

    public static final Gson GSON = new Gson();

    String token = System.getenv("OPENAI_TOKEN");
    OpenAiService service = new OpenAiService(token, Duration.ofSeconds(30L));
    @Override
    public SendVideo ask(Update update, DefaultAbsSender sender) throws Exception {
        if (CollectionUtils.isEmpty(update.getMessage().getPhoto()))
            throw new RuntimeException("I need a photo to create avatar");
        PhotoSize photo = update.getMessage().getPhoto().get(2);
        Long chatId = update.getMessage().getChatId();
        GetFile getFile = new GetFile();
        getFile.setFileId(photo.getFileId());
        File filePath = sender.execute(getFile);
        File file = new File(filePath.getFileId(), filePath.getFileUniqueId(), filePath.getFileSize(), filePath.getFilePath());
        String fileUrl = file.getFileUrl(botConfig.getTg_token());
        DidAnimationTaskResultDTO didAnimationTask = d_idRequestService.sendDidAnimationTask(fileUrl);
        sleep(10000);
        DidResultInfoDTO resultInfoDTO = d_idRequestService.requestDidResultInfo(didAnimationTask.getId());
        return answerService.didVideoUrl(resultInfoDTO.getResult_url(), chatId);
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }
}
