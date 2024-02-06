package com.wcg.chargen.backend.controller;

import com.wcg.chargen.backend.model.Professions;
import com.wcg.chargen.backend.service.impl.DefaultProfessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/professions")
public class ProfessionController {
    @Autowired
    DefaultProfessionService professionService;

    @GetMapping("")
    public ResponseEntity<Professions> getProfessions() {
        try {
            var professionList = professionService.getAllProfessions();

            return new ResponseEntity<>(professionList, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
