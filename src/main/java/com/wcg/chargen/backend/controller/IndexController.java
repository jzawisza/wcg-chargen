package com.wcg.chargen.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Allow loading of frontend from Spring Boot JAR without having to explicitly specify /index.html
 * in URL, i.e. localhost:5000/ instead of localhost:5000/index.html
 */
@Controller
public class IndexController {
    @GetMapping("/")
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
