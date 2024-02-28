package com.wcg.chargen.backend.controller;

import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.service.impl.DefaultSkillsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/skills")
public class SkillsController {
    @Autowired
    DefaultSkillsService skillsService;

    @GetMapping("")
    public ResponseEntity<Skills> getSkills() {
        try {
            var skillsList = skillsService.getAllSkills();

            return new ResponseEntity<>(skillsList, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
