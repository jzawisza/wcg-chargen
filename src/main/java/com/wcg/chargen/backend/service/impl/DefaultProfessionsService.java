package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.Profession;
import com.wcg.chargen.backend.model.Professions;
import com.wcg.chargen.backend.service.ProfessionsService;
import com.wcg.chargen.backend.service.YamlLoaderService;
import com.wcg.chargen.backend.worker.RandomNumberWorker;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@Service
public class DefaultProfessionsService implements ProfessionsService {
    Logger logger = LoggerFactory.getLogger(DefaultProfessionsService.class);

    private final YamlLoaderService<Professions> yamlLoaderService;
    private final RandomNumberWorker randomNumberWorker;

    @Autowired
    public DefaultProfessionsService(YamlLoaderService<Professions> yamlLoaderService,
                                     RandomNumberWorker randomNumberWorker) {
        this.yamlLoaderService = yamlLoaderService;
        this.randomNumberWorker = randomNumberWorker;
    }

    private Professions professions;
    private final Profession[] professionTable = new Profession[99];

    @PostConstruct
    private void postConstruct() {
        // Since the YAML loader service is autowired, we need to do this
        // after the bean has been constructed
        professions = yamlLoaderService.loadFromYaml();
        if (professions == null) {
            throw new IllegalStateException("Error loading professions YAML file");
        }

        // Populate profession table based on YAML file contents
        for (var profession : professions.professions()) {
            // Convert from 1-based to 0-based numbers to populate array
            int rangeStart = profession.rangeStart() - 1;
            int rangeEnd = profession.rangeEnd() - 1;

            if (rangeStart == rangeEnd) {
                professionTable[rangeStart] = profession;
            }
            else {
                for (var i = rangeStart; i <= rangeEnd; i++) {
                    professionTable[i] = profession;
                }
            }
        }

        // Validate that profession table is fully populated
        var hasMissingElements = Arrays.stream(professionTable).anyMatch(Objects::isNull);
        if (hasMissingElements) {
            throw new IllegalStateException("Professions table has missing elements");
        }
    }

    @Override
    public Professions getAllProfessions() {
        return professions;
    }

    @Override
    public Professions generateRandomProfessions() {
        var professionList = new ArrayList<Profession>();
        int professionRoll = randomNumberWorker.getIntFromRange(1, 99);
        logger.info("Rolled {} for professions roll", professionRoll);

        if (isPalindromeNumber(professionRoll)) {
            // Return the profession rolled plus the one before and the one after
            // *in the master list*, not numerically
            // For example, if 33 was rolled, we would return Entertainer, Farmer, and Fisherman,
            // not the professions for 32 and 34 (which are also Farmer)

            // Subtract 1 from numbers since array is 0-based
            var profession = professionTable[professionRoll - 1];

            var professionIndex = professions.professions().indexOf(profession);
            var professionBefore = professions.professions().get(professionIndex - 1);
            var professionAfter = professions.professions().get(professionIndex + 1);

            professionList.add(professionBefore);
            professionList.add(profession);
            professionList.add(professionAfter);
        }
        else {
            int reverseRoll = reverseInteger(professionRoll);

            // Subtract 1 from numbers since array is 0-based
            professionList.add(professionTable[professionRoll - 1]);
            professionList.add(professionTable[reverseRoll - 1]);
        }

        return new Professions(professionList);
    }

    /**
     * @param num Number between 1 and 99
     * @return True if the number has two digits and both are the same (e.g. 22, 55), false otherwise.
     */
    private boolean isPalindromeNumber(int num) {
        // Two-digit numbers are palindromes if they're divisible by 11
        return num % 11 == 0;
    }

    /**
     *
     * @param num Integer between 1 and 99
     * @return
     */
    private int reverseInteger(int num) {
        if (num < 10) {
            return num * 10;
        }
        else if (num % 10 == 0) {
            return num / 10;
        }
        else {
            var tensDigit = num / 10;
            var onesDigit = num % 10;

            return onesDigit * 10 + tensDigit;
        }
    }
}
