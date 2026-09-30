package com.placement.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class WebAppController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<FileSystemResource> index() {
        File file = new File("webapp/index.html");
        if (!file.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new FileSystemResource(file));
    }
}
