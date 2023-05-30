package com.example.gpt.entity;

import lombok.Data;

@Data
public class DidResultInfoDTO {
    private String id;
    private String created_at;
    private String created_by;
    private String status;
    private String result_url;
    private String driver_url;
}
