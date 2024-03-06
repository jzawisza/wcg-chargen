package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.model.Skill;
import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.model.SkillsResponse;
import com.wcg.chargen.backend.service.SkillsService;
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

    private final DefaultCharClassesService charClassesService;

    private final HashMap<String, Skill> skillsMap = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(DefaultSkillsService.class);

    private static final SkillsResponse EMPTY_RESPONSE = new SkillsResponse();

    @Autowired
    public DefaultSkillsService(YamlLoaderService<Skills> yamlLoaderService,
                                DefaultCharClassesService charClassesService) {
        this.yamlLoaderService = yamlLoaderService;
        this.charClassesService = charClassesService;
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
    public SkillsResponse getSkills(CharType charType) {
        logger.info("Getting skills for character class {}", charType);

        var response = new SkillsResponse();
        var charClass = charClassesService.getCharClassByType(charType);
        // Use TreeSets in this method to sort the values
        var charClassSkillSet = new TreeSet<>(charClass.skills());

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

        // The overall skills remaining in the list will constitute the bonus skills,
        // i.e. other skills a player can choose when creating their character
        for (var bonusSkill : allSkillSet) {
            var skillObj = skillsMap.get(bonusSkill);
            if (skillObj == null) {
                logger.error("No skill object found for bonus skill {}", bonusSkill);
                return EMPTY_RESPONSE;
            }

            response.addBonusSkill(skillObj);
        }

        return response;
    }
}
