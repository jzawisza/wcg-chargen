import { render, screen } from "@testing-library/react";
import Skills from "../Skills";
import { CharacterContext } from "../../../Context";
import { SkillsType } from "../../../server/SkillsType";
import * as skills from "../../../server/ServerData";

const MOCK_SKILLS_DATA: SkillsType = {
    classSkills: [
        {
            "name": "Culture",
            "attributes": [
                "INT"
            ]
        }
    ],
    speciesSkills: [
        {
            "name": "Culture",
            "attributes": [
                "INT"
            ]
        }
    ],
    bonusSkills: [
        {
            "name": "Culture",
            "attributes": [
                "INT"
            ]
        }
    ]
};

const MOCK_EMPTY_SKILLS_DATA: SkillsType = {
    classSkills: [],
    speciesSkills: [],
    bonusSkills: []
};

test('expected elements displayed for humans', () => {
    jest.spyOn(skills, 'useSkillsData').mockReturnValue({
        data: MOCK_SKILLS_DATA,
        error: false,
        isLoading: false
    });

    const speciesContext = {
        species: 'human'
    };

    render(
        <CharacterContext.Provider value={speciesContext}>
            <Skills />
        </CharacterContext.Provider>
    );

    const classSkillsHeader = screen.getByText('Class Skills');
    const speciesBonusSkillsHeader = screen.getByText('Species and Bonus Skills');
    const selectPlaceholderText = screen.getByText("Select 2 skills");

    expect(classSkillsHeader).toBeTruthy();
    expect(speciesBonusSkillsHeader).toBeTruthy();
    expect(selectPlaceholderText).toBeTruthy();
});

test.each([
    ["dwarf"],
    ["elf"],
    ["halfling"]
])('expected elements displayed for non-human species %s', (speciesStr) => {
    jest.spyOn(skills, 'useSkillsData').mockReturnValue({
        data: MOCK_SKILLS_DATA,
        error: false,
        isLoading: false
    });

    const speciesContext = {
        species: speciesStr
    };

    const { container } = render(
        <CharacterContext.Provider value={speciesContext}>
            <Skills />
        </CharacterContext.Provider>
    );

    const classSkillsHeader = screen.getByText('Class Skills');
    const speciesSkillsHeader = screen.getByText('Species Skill');
    const bonusSkillHeader = screen.getByText('Bonus Skill');
    const selectPlaceholderText = screen.getByText("Select 1 skill");
    const speciesSkillRadioGroup = container.querySelector('.ant-radio-group');

    expect(classSkillsHeader).toBeTruthy();
    expect(speciesSkillsHeader).toBeTruthy();
    expect(bonusSkillHeader).toBeTruthy();
    expect(selectPlaceholderText).toBeTruthy();
    expect(speciesSkillRadioGroup).toBeTruthy();
});

test('error from server displays expected output', () => {
    jest.spyOn(skills, 'useSkillsData').mockReturnValue({
        data: MOCK_EMPTY_SKILLS_DATA,
        error: true,
        isLoading: false
    });

    render(<Skills />);

    const errorText = screen.getByText('Error loading skills data from server.');

    expect(errorText).toBeTruthy();
});

test('data loading from server displays expected output', () => {
    jest.spyOn(skills, 'useSkillsData').mockReturnValue({
        data: MOCK_EMPTY_SKILLS_DATA,
        error: false,
        isLoading: true
    });

    const { container } = render(<Skills />);

    const spinner = container.querySelector('.ant-spin');

    expect(spinner).toBeTruthy();
});