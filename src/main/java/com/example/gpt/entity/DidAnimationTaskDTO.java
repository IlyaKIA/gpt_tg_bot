package com.example.gpt.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DidAnimationTaskDTO {
    private String source_url;
    private String driver_url;
    private DidConfigDTO config;
}
