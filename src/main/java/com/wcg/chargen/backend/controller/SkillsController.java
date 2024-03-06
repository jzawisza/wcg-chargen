package com.wcg.chargen.backend.controller;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.service.impl.DefaultSkillsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/skills")
public class SkillsController {
    @Autowired
    private DefaultSkillsService skillsService;

    private final Logger logger = LoggerFactory.getLogger(SkillsController.class);

    @GetMapping("")
    public ResponseEntity getSkills(@RequestParam String charClass) {
        try {
            // Normalize input string to uppercase to match enum definition
            var charType = CharType.valueOf(charClass.toUpperCase());

            var skillsResponse = skillsService.getSkills(charType);

            return new ResponseEntity<>(skillsResponse, HttpStatus.OK);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid charClass argument {} passed in to endpoint", charClass);
            return new ResponseEntity<>("Invalid charClass argument " + charClass, HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            logger.error("Exception thrown when retrieving skills information", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
