package com.wcg.chargen.backend.service.impl.charCreate;

import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.service.CharacterCreateService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseCharacterCreateService implements CharacterCreateService {
    @Autowired
    CharacterCreateRequestValidatorService characterCreateRequestValidatorService;

    public CharacterCreateStatus createCharacter(CharacterCreateRequest characterCreateRequest, String bearerToken) {
        var status = characterCreateRequestValidatorService.validate(characterCreateRequest);
        if (!status.isSuccess()) {
            // If the request isn't valid, abort here
            return status;
        }

        return doCreateCharacter(characterCreateRequest, bearerToken);
    }

    public abstract CharacterCreateStatus doCreateCharacter(CharacterCreateRequest characterCreateRequest, String bearerToken);
}
