import React, { useContext, useEffect } from "react";
import { NextButtonEnabledContext, CharacterContext } from "../../Context";
import { Row, Col, Radio, RadioChangeEvent, List, Spin } from "antd";
import { HUMAN_SPECIES_INFO, getPluralSpeciesNameFromVariable, getIsHuman } from "../../constants/SpeciesInfo";
import SelectMultiple from "../util/SelectMultiple";
import { useSkillsData } from "../../server/ServerData";
import { SkillType } from "../../server/SkillsType";

function skillEltToString(skillElt: SkillType) {
    return `${skillElt.name} (${skillElt.attributes.join(',')})`
}

function toClassSkillList(classSkills: SkillType[] | undefined) {
    if (!classSkills) {
        return [];
    }

    return classSkills.map(x => skillEltToString(x));
}

function toSpeciesSkillRadioGroup(speciesSkills: SkillType[] | undefined) {
    if (!speciesSkills) {
        return [];
    }

    return speciesSkills.map(x => (<Radio.Button value={x.name}>{skillEltToString(x)}</Radio.Button>));
}

function toBonusSkillsOptionList(bonusSkills : SkillType[] | undefined) {
    if (!bonusSkills) {
        return [];
    }

    return bonusSkills.map(x => ({value: x.name, label: skillEltToString(x)}));
}

function getShouldEnableNext(speciesInternalName: string, speciesSkill: string, bonusSkills: string[]) {
    // If we deselect everything from the bonus skills list, this value is undefined
    if (bonusSkills === undefined) {
        return false;
    }

    if (speciesInternalName === HUMAN_SPECIES_INFO.internalName) {
        // Humans can pick any skill as their species skill, so they have enough information
        // if they select two skills from the list
        return (typeof(bonusSkills) === "object" && bonusSkills.length === 2);
    }
    
    // For other species, they need one bonus skill plus one of their designated species skills
    // Ant Design does something weird where bonusSkills is handled as a string instead of a singleton array,
    // so this method includes an implementation specific to that quirk plus a normal implementation
    if (typeof(bonusSkills) === "string") {
        return bonusSkills !== '' && speciesSkill !== '';
    }
    else {
        return bonusSkills.length === 1 &&  speciesSkill !== '';
    }
}

const Skills: React.FC = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);
    const { charClass, species, speciesSkill, setSpeciesSkill,
                bonusSkills, setBonusSkills } = useContext(CharacterContext);
    const { data, error, isLoading } = useSkillsData(charClass, species);

    const isHuman = getIsHuman(species);

    useEffect(() => {
        let shouldEnableNext = getShouldEnableNext(species, speciesSkill, bonusSkills);
        setNextEnabled(shouldEnableNext);
    }, [species, speciesSkill, bonusSkills, setNextEnabled]);

    const onSpeciesSkillRadioGroupChange = (e: RadioChangeEvent) => {
        let newSpeciesSkill = e.target.value;

        setSpeciesSkill(newSpeciesSkill);

        let shouldEnableNext = getShouldEnableNext(species, newSpeciesSkill, bonusSkills);
        setNextEnabled(shouldEnableNext);
    };

    const onBonusSkillsChange = (value: string[]) => {
        setBonusSkills(value);

        let shouldEnableNext = getShouldEnableNext(species, speciesSkill, value);
        setNextEnabled(shouldEnableNext);
    }

    const classSkillColSpan = isHuman ? 8 : 6;
    const otherSkillColSpan = isHuman ? 12 : 6;

    if (error) {
        return (
            <p>Error loading skills data from server.</p>
        );
    }
    if (isLoading) {
        return (
            <Spin size="large" />
        );
    }

    return (
        <div>
            <p>Each class gets certain skills for free, but you may select any one bonus skill, plus another bonus skill based on your species.</p>
            <Row justify="center">
                <Col span={classSkillColSpan} className="skillsCol">
                    <h3>Class Skills</h3>
                    <List
                        dataSource={toClassSkillList(data?.classSkills)}
                        renderItem={(item) => (
                            <List.Item>
                                {item}
                            </List.Item>
                        )}
                    />
                </Col>
                <Col span={otherSkillColSpan} className="skillsCol">
                    {!isHuman && (
                        <div>
                            <h3>Species Skill</h3>
                            <p>{getPluralSpeciesNameFromVariable(species)} get one of the following skills for free.</p>
                            <Radio.Group buttonStyle="solid" onChange={onSpeciesSkillRadioGroupChange} value={speciesSkill}>
                                {toSpeciesSkillRadioGroup(data?.speciesSkills)}
                            </Radio.Group>
                        </div>
                    )}
                    {isHuman && (
                        <div>
                            <h3>Species and Bonus Skills</h3>
                            <p>Humans can take any skill for free as their species skill.  Therefore, you may select two skills from the list below, i.e. your species skill and your bonus skill.</p>
                            <SelectMultiple
                                defaultValue={bonusSkills}
                                numElementsAllowed={2}
                                onChange={onBonusSkillsChange}
                                options={toBonusSkillsOptionList(data?.bonusSkills)}
                                placeholder="Select 2 skills"
                            />
                        </div>
                    )}
                </Col>
                {!isHuman && (
                    <Col span={otherSkillColSpan} className="skillsCol">
                        <div>
                            <h3>Bonus Skill</h3>
                            <p>Select one bonus skill from the following list.</p>
                            <SelectMultiple
                                defaultValue={bonusSkills}
                                numElementsAllowed={1}
                                onChange={onBonusSkillsChange}
                                options={toBonusSkillsOptionList(data?.bonusSkills)}
                                placeholder="Select 1 skill"
                            />
                        </div>
                    </Col>
                )}
            </Row>
        </div>
    );
};

export default Skills;