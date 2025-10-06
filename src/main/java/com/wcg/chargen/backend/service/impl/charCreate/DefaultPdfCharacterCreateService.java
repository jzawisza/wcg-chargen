package com.wcg.chargen.backend.service.impl.charCreate;

import com.wcg.chargen.backend.constants.PdfFieldConstants;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.PdfCharacterCreateStatus;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.service.PdfCharacterCreateService;
import com.wcg.chargen.backend.util.CharacterSheetUtil;
import com.wcg.chargen.backend.util.PdfUtil;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class DefaultPdfCharacterCreateService implements PdfCharacterCreateService {
    private static final String PDF_FILE_NAME = "charSheet.pdf";

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

        try (var inputStream = getClass().getClassLoader().getResourceAsStream(PDF_FILE_NAME);
             var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(inputStream));
             var outputStream = new ByteArrayOutputStream()) {
            // Set basic information
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.CHARACTER_NAME, request.characterName());
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.LEVEL, String.valueOf(request.level()));
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.SPECIES, request.species().toCharSheetString());

            pdfDocument.save(outputStream);
            var returnInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            var pdfFileName = CharacterSheetUtil.generateName(request) + ".pdf";
            return new PdfCharacterCreateStatus(returnInputStream, pdfFileName, null);
        }
        catch (Exception e) {
            logger.error("Error creating PDF character sheet", e);
            return PdfCharacterCreateStatus.error("Error creating PDF character sheet");
        }
    }
}
