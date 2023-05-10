package com.example.gpt.d_id.controller;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;

@Controller
@RequestMapping(value = "/img")
public class FileController {

    @GetMapping(value = "{fileName}",
            produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getPhoto(@PathVariable String fileName) throws IOException {
        return IOUtils.toByteArray(new FileInputStream("C:\\var\\img\\" + fileName));
    }
}
