package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.SkillsResponse;
import com.wcg.chargen.backend.service.*;
import com.wcg.chargen.backend.worker.SkillsProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.TreeSet;

@Service
public class DefaultSkillsService implements SkillsService {
    private final CharClassesService charClassesService;

    private final SpeciesService speciesService;

    private final SkillsProvider skillsProvider;

    private final Logger logger = LoggerFactory.getLogger(DefaultSkillsService.class);

    private static final SkillsResponse EMPTY_RESPONSE = new SkillsResponse();

    @Autowired
    public DefaultSkillsService(SkillsProvider skillsProvider,
                                CharClassesService charClassesService,
                                SpeciesService speciesService) {
        this.skillsProvider = skillsProvider;
        this.charClassesService = charClassesService;
        this.speciesService = speciesService;
    }

    @Override
    public SkillsResponse getSkills(CharType charType, SpeciesType speciesType) {
        logger.info("Getting skills for character class {} and species {}", charType, speciesType);

        var response = new SkillsResponse();
        var charClass = charClassesService.getCharClassByType(charType);
        var species = speciesService.getSpeciesByType(speciesType);

        // Use TreeSets in this method to sort the values
        var charClassSkillSet = new TreeSet<>(charClass.skills());
        // The species-specific skills are null for humans, since they can take any skill
        // as a bonus skill
        var speciesSkillSet = (species.skills() != null) ?
                new TreeSet<>(species.skills()) :
                new TreeSet<String>();

        // Set containing the names of all skills
        var allSkillSet = skillsProvider.getSkillNameSet();

        // Add skill object for each class skill to the response,
        // and remove that object from the overall list
        for (var classSkill : charClassSkillSet) {
            var skillObj = skillsProvider.getByName(classSkill);
            if (skillObj == null) {
                logger.error("No skill object found for class skill {}", classSkill);
                return EMPTY_RESPONSE;
            }
            response.addClassSkill(skillObj);
            allSkillSet.remove(classSkill);
        }

        for (var speciesSkill : speciesSkillSet) {
            // Don't add any species skills that are already part of the character class skills,
            // as players should take an unique species skill.
            // For example, an elf shaman should not have Arcana or Nature in their species skill list,
            // since they already get them as class skills.
            if (!charClassSkillSet.contains(speciesSkill)) {
                var skillObj = skillsProvider.getByName(speciesSkill);
                if (skillObj == null) {
                    logger.error("No skill object found for species skill {}", speciesSkill);
                    return EMPTY_RESPONSE;
                }

                response.addSpeciesSkill(skillObj);
            }
        }

        // The overall skills remaining in the list will constitute the bonus skills,
        // i.e. other skills a player can choose when creating their character
        for (var bonusSkill : allSkillSet) {
            var skillObj = skillsProvider.getByName(bonusSkill);
            // This is purely defensive coding: the condition can never be true,
            // since allSkillSet is the result of calling skillsMap.keySet()
            if (skillObj == null) {
                logger.error("No skill object found for bonus skill {}", bonusSkill);
                return EMPTY_RESPONSE;
            }

            response.addBonusSkill(skillObj);
        }

        return response;
    }
}
