package com.wcg.chargen.backend.service.impl;

import com.sun.source.tree.Tree;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.Skill;
import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.model.SkillsResponse;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.SkillsService;
import com.wcg.chargen.backend.service.SpeciesService;
import com.wcg.chargen.backend.service.YamlLoaderService;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.TreeSet;

@Service
public class DefaultSkillsService implements SkillsService {
    private final YamlLoaderService<Skills> yamlLoaderService;

    private final CharClassesService charClassesService;

    private final SpeciesService speciesService;

    private final HashMap<String, Skill> skillsMap = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(DefaultSkillsService.class);

    private static final SkillsResponse EMPTY_RESPONSE = new SkillsResponse();

    @Autowired
    public DefaultSkillsService(YamlLoaderService<Skills> yamlLoaderService,
                                CharClassesService charClassesService,
                                SpeciesService speciesService) {
        this.yamlLoaderService = yamlLoaderService;
        this.charClassesService = charClassesService;
        this.speciesService = speciesService;
    }

    @PostConstruct
    private void postConstruct() {
        var skills = yamlLoaderService.loadFromYaml();
        if (skills == null) {
            throw new IllegalStateException("Error loading skills YAML file");
        }

        // Initialize map from skill name to skill object
        for (var skill : skills.skills()) {
            skillsMap.put(skill.name(), skill);
        }
    }

    @Override
    public SkillsResponse getSkills(CharType charType, SpeciesType speciesType) {
        logger.info("Getting skills for character class {} and species {}", charType, speciesType);

        var response = new SkillsResponse();
        var charClass = charClassesService.getCharClassByType(charType);
        var species = speciesService.getSpeciesByType(speciesType);

        // Use TreeSets in this method to sort the values
        var charClassSkillSet = new TreeSet<>(charClass.skills());
        var speciesSkillSet = new TreeSet<>(species.skills());

        // Set containing the names of all skills
        var allSkillSet = new TreeSet<>(skillsMap.keySet());

        // Add skill object for each class skill to the response,
        // and remove that object from the overall list
        for (var classSkill : charClassSkillSet) {
            var skillObj = skillsMap.get(classSkill);
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
                var skillObj = skillsMap.get(speciesSkill);
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
            var skillObj = skillsMap.get(bonusSkill);
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
