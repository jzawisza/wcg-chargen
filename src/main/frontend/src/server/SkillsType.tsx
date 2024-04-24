export interface SkillType {
    name: string,
    attributes: string[]
}

export interface SkillsType {
    classSkills: SkillType[],
    speciesSkills: SkillType[],
    bonusSkills: SkillType[]
}