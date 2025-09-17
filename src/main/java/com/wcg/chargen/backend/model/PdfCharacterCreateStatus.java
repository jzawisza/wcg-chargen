package com.wcg.chargen.backend.model;

import java.io.InputStream;

public record PdfCharacterCreateStatus(InputStream pdfStream, String fileName, String errMsg) {
    public static PdfCharacterCreateStatus error(String errMsg) {
        return new PdfCharacterCreateStatus(null, null, errMsg);
    }
}
