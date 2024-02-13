package com.jeongjy.springaoptest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api")
@RestController
public class TestController {

    @GetMapping("/get")
    public String get(@RequestParam String name) {

        return name;
    }

}
