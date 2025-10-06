package com.wcg.chargen.backend.util;

import com.wcg.chargen.backend.model.CharacterCreateRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CharacterSheetUtil {
    /**
     * Generate a name for the character sheet, i.e. the Google Sheets title or PDF file name.
     *
     * @param request Character create request
     * @return Name for the character sheet
     */
    public static String generateName(CharacterCreateRequest request) {
        var currentDateTime = LocalDateTime.now();
        var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // For character names, replace spaces and characters that are not allowed in file names
        // with underscores
        var characterName = (request.characterName() != null) ?
                request.characterName().replaceAll("[\\s\\\\/:*?\"<>|]", "_") :
                "";

        // For professions, replace spaces and slashes with underscores
        // to simplify their representation
        var classOrProfession = request.isCommoner() ?
                request.profession().replaceAll("\\s|/", "_").toUpperCase() :
                request.characterClass().toString().toUpperCase();

        return String.format("%s_%s_%s_%s",
                characterName,
                request.species().toString().toUpperCase(),
                classOrProfession,
                currentDateTime.format(dateTimeFormatter));
    }
}
