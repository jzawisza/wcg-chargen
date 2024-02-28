package com.wcg.chargen.backend.service;

import com.wcg.chargen.backend.model.Professions;

public interface ProfessionsService {
    Professions getAllProfessions();
    Professions generateRandomProfessions();
}