package com.example.gpt.service;

import com.example.gpt.entity.DidAnimationTaskDTO;
import com.example.gpt.entity.DidAnimationTaskResultDTO;
import com.example.gpt.entity.DidConfigDTO;
import com.example.gpt.entity.DidResultInfoDTO;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class DIdRequestService {
    public static final String ERROR_WHILE_SENDING_REQUEST_TO_D_ID_API = "Error while sending request to D-ID API:";
    private final String baseUrl = "https://api.d-id.com/";
    private final String authorization = System.getenv("DID_TOKEN");
    private final Gson GSON = new Gson();

    public DidAnimationTaskResultDTO sendDidAnimationTask(String fileUrl) throws Exception {
        DidAnimationTaskDTO task = DidAnimationTaskDTO.builder()
                .source_url(fileUrl)
                .config(DidConfigDTO.builder()
                        .stitch(true)
                        .build())
                .build();
        HttpResponse<String> response = sendRequest("animations", "POST",
                HttpRequest.BodyPublishers.ofString(GSON.toJson(task)));
        if (HttpStatus.CREATED.value() == response.statusCode()) {
            return GSON.fromJson(response.body(), DidAnimationTaskResultDTO.class);
        } else {
            throw new Exception(ERROR_WHILE_SENDING_REQUEST_TO_D_ID_API + " " + response.body());
        }
    }

    public DidResultInfoDTO requestDidResultInfo(String id) throws Exception {
        HttpResponse<String> response = sendRequest("animations/" + id, "GET", HttpRequest.BodyPublishers.noBody());
        if (HttpStatus.OK.value() == response.statusCode()) {
            return GSON.fromJson(response.body(), DidResultInfoDTO.class);
        } else {
            throw new Exception(ERROR_WHILE_SENDING_REQUEST_TO_D_ID_API + " " + response.body());
        }
    }

    private HttpResponse<String> sendRequest(String urlPostfix, String method, HttpRequest.BodyPublisher bodyPublisher)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + urlPostfix))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("authorization", "Basic " + authorization)
                .method(method, bodyPublisher)
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
