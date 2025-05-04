package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.service.ProfessionsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultCharacterCreateRequestValidatorService implements CharacterCreateRequestValidatorService {
    @Autowired
    ProfessionsService professionsService;

    public CharacterCreateStatus validate(CharacterCreateRequest characterCreateRequest) {
        if (characterCreateRequest == null) {
            return failedStatus("Invalid object");
        }
        if (StringUtils.isEmpty(characterCreateRequest.characterName())) {
            return failedStatus("Missing character name");
        }
        if (characterCreateRequest.species() == null) {
            return failedStatus("Missing species");
        }
        if (characterCreateRequest.level() == null) {
            return failedStatus("Missing level");
        }

        var level = characterCreateRequest.level();
        if (level < 0 || level > 7) {
            return failedStatus("Level must be between 0 and 7");
        }
        if (level == 0) {
            // A level 0 character should have a profession and not a class
            if (characterCreateRequest.characterClass() != null) {
                return failedStatus("Level 0 characters cannot have a character class");
            }

            var profession = characterCreateRequest.profession();
            if (StringUtils.isEmpty(profession)) {
                return failedStatus("Level 0 characters must have a profession");
            }

            var isProfessionValid = professionsService.getAllProfessions().professions().stream()
                    .anyMatch(x -> x.name().equals(profession));
            if (!isProfessionValid) {
                return failedStatus("Profession " + profession + " is not a valid profession");
            }
        }
        else if (characterCreateRequest.characterClass() == null) {
            // Characters with levels 1-7 must have a class
            return failedStatus("Missing character class");
        }

        return CharacterCreateStatus.SUCCESS;
    }

    private CharacterCreateStatus failedStatus(String msg) {
        return new CharacterCreateStatus(false, msg);
    }
}
