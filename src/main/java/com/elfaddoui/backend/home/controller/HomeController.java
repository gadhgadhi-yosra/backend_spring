package com.elfaddoui.backend.home.controller;

import com.elfaddoui.backend.home.dto.HomeResponse;
import com.elfaddoui.backend.home.service.HomeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/home", produces = MediaType.APPLICATION_JSON_VALUE)
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    public HomeResponse getHome() {
        return homeService.getHome();
    }
}
