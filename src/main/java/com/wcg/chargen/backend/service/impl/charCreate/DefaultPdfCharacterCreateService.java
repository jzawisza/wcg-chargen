package com.wcg.chargen.backend.service.impl.charCreate;

import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.PdfCharacterCreateStatus;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.service.PdfCharacterCreateService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class DefaultPdfCharacterCreateService implements PdfCharacterCreateService {
    private final Logger logger = LoggerFactory.getLogger(DefaultPdfCharacterCreateService.class);

    @Autowired
    CharacterCreateRequestValidatorService characterCreateRequestValidatorService;

    @Override
    public PdfCharacterCreateStatus createCharacter(CharacterCreateRequest request) {
        var status = characterCreateRequestValidatorService.validate(request);
        if (!status.isSuccess()) {
            // If the request isn't valid, abort here
            return PdfCharacterCreateStatus.error(status.message());
        }

        try (var inputStream = getClass().getClassLoader().getResourceAsStream("charSheet.pdf");
             var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(inputStream));
             var outputStream = new ByteArrayOutputStream()) {
            pdfDocument.save(outputStream);
            var returnInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            return new PdfCharacterCreateStatus(returnInputStream, "charSheet.pdf", null);
        }
        catch (Exception e) {
            logger.error("Error creating PDF character sheet", e);
            return PdfCharacterCreateStatus.error("Error creating PDF character sheet");
        }
    }
}
