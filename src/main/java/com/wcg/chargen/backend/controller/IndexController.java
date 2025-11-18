package com.wcg.chargen.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Allow loading of frontend from Spring Boot JAR without having to explicitly specify /index.html
 * in URL, i.e. localhost:8080/ instead of localhost:8080/index.html
 */
@Controller
public class IndexController {
    @GetMapping("")
    public ModelAndView home() {
        return new ModelAndView("index");
    }
}
