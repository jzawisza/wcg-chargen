package com.wcg.chargen.backend.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfUtil {
    private static final Logger logger = LoggerFactory.getLogger(PdfUtil.class);

    private static PDField lookUpField(PDDocument document, String fieldName) {
        var docCatalog = document.getDocumentCatalog();
        var acroForm = docCatalog.getAcroForm();
        var field = acroForm.getField(fieldName);

        if (field == null) {
            logger.warn("No field found with name {}", fieldName);
        }

        return field;
    }

    public static void setFieldValue(PDDocument document, String fieldName, String value) {
        var field = lookUpField(document, fieldName);
        if (field != null) {
            try {
                field.setValue(value);
            } catch (Exception e) {
                logger.error("Error setting field {} to value {}", fieldName, value, e);
            }
        }
    }

    public static String getFieldValue(PDDocument document, String fieldName) {
        var field = lookUpField(document, fieldName);

        return (field != null) ? field.getValueAsString() : "";
    }
}
