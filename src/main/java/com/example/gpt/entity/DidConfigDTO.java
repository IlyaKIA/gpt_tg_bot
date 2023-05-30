package com.example.gpt.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DidConfigDTO {
    private Double max_animated_faces;
    private String result_format;
    private Boolean stitch;

}
