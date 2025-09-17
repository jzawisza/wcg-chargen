package com.wcg.chargen.backend.controller;

import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.service.PdfCharacterCreateService;
import com.wcg.chargen.backend.service.impl.charCreate.GoogleSheetsCharacterCreateService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("api/v1/createcharacter")
public class CharacterCreateController {
    @Autowired
    GoogleSheetsCharacterCreateService googleSheetsCharacterCreateService;
    @Autowired
    PdfCharacterCreateService pdfCharacterCreateService;

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

    @PostMapping("pdf")
    public ResponseEntity<InputStreamResource> createCharacterPdf
            (@Valid @RequestBody CharacterCreateRequest characterCreateRequest) throws IOException {
        InputStream pdfStream = null;
        try {
            var status = pdfCharacterCreateService.createCharacter(characterCreateRequest);
            if (status.pdfStream() != null) {
                pdfStream = status.pdfStream();
                var resource = new InputStreamResource(pdfStream);
                var headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + status.fileName());
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            }
            else {
                var errorMsgInputStream = new ByteArrayInputStream(status.errMsg().getBytes(StandardCharsets.UTF_8));
                var errorResource = new InputStreamResource(errorMsgInputStream);
                return new ResponseEntity<>(errorResource, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        catch (Exception e) {
            logger.error("Exception thrown when creating PDF character sheet", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        finally {
            if(pdfStream != null) {
                pdfStream.close();
            }
        }
    }
}
