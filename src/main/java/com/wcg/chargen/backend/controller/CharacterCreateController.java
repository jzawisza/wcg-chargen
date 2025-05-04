package com.wcg.chargen.backend.controller;

import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.service.impl.charCreate.GoogleSheetsCharacterCreateService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/createcharacter")
public class CharacterCreateController {
    @Autowired
    GoogleSheetsCharacterCreateService googleSheetsCharacterCreateService;

    private final Logger logger = LoggerFactory.getLogger(CharacterCreateController.class);

    @PostMapping("googlesheets")
    public ResponseEntity<String> createCharacterGoogle(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String bearerToken,
                                          @Valid @RequestBody CharacterCreateRequest characterCreateRequest) {
        try {
            var status = googleSheetsCharacterCreateService.createCharacter(characterCreateRequest, bearerToken);
            if (status.isSuccess()) {
                return new ResponseEntity<>("Success!", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(status.message(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        catch (Exception e) {
            logger.error("Exception thrown when creating character", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
