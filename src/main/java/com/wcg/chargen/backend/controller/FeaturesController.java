package com.wcg.chargen.backend.controller;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.service.impl.DefaultCharClassesService;
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
@RequestMapping("api/v1/features")
public class FeaturesController {
    @Autowired
    private DefaultCharClassesService charClassesService;

    private final Logger logger = LoggerFactory.getLogger(FeaturesController.class);

    @GetMapping("")
    public ResponseEntity getFeatures(@RequestParam String charClass) {
        try {
            // Normalize input string to uppercase to match enum definition
            var charType = CharType.valueOf(charClass.toUpperCase());

            var charInfo = charClassesService.getCharClassByType(charType);

            return new ResponseEntity<>(charInfo.features(), HttpStatus.OK);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid character class {} passed in to endpoint", charClass);
            return new ResponseEntity<>("Invalid query parameters specified", HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            logger.error("Exception thrown when retrieving features information", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
