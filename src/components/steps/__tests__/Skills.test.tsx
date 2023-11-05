import { render, screen } from "@testing-library/react";
import Skills from "../Skills";
import { CharacterContext } from "../../../Context";

test('expected elements displayed for humans', () => {
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
])('expected elements displayed for non-human species', (speciesStr) => {
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