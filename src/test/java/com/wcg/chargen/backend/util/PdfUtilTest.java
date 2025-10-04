package com.wcg.chargen.backend.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Contains tests for functionality in PdfUtil that is not covered by the
 * DefaultPdfCharacterCreateServiceTests class.
 */
public class PdfUtilTest {
    @Test
    public void getFieldValue_ReturnsEmptyStringForNonExistentField() throws Exception {
        // arrange
        try (var inputStream = getClass().getClassLoader().getResourceAsStream("charSheet.pdf");
             var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {

            // act
            var fieldValue = PdfUtil.getFieldValue(pdfDocument, "NonExistentField");

            // assert
            assertNotNull(fieldValue);
            assertEquals("", fieldValue);
        }
    }

    @Test
    public void setFieldValue_DoesNotThrowExceptionForNonExistentField() throws Exception {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream("charSheet.pdf");
             var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {
            PdfUtil.setFieldValue(pdfDocument, "NonExistentField", "SomeValue");
        }
    }
}
