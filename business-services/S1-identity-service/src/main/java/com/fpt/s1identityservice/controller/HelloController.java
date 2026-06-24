package com.fpt.s1identityservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/s1") // Đặt gạch đầu dòng phân biệt của Service 1
public class HelloController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World từ S1-Identity-Service thực hiện thành công!";
    }
}