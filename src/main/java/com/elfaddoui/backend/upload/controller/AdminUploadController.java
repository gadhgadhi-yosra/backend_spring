package com.elfaddoui.backend.upload.controller;

import com.elfaddoui.backend.upload.dto.UploadImageResponse;
import com.elfaddoui.backend.upload.service.ImageStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/api/admin/uploads", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminUploadController {

    private final ImageStorageService imageStorageService;

    public AdminUploadController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UploadImageResponse upload(@RequestParam("file") MultipartFile file) {
        return new UploadImageResponse(imageStorageService.store(file));
    }
}
