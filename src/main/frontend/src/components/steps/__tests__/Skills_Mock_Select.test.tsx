import { render, screen, act, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { DefaultOptionType } from "antd/es/select";
import Skills from "../Skills";
import { CharacterContext, NextButtonEnabledContext } from "../../../Context";
import { SkillsType } from "../../../server/SkillsType";
import * as skills from "../../../server/ServerData";

/**
 * Tests for the Skills component that require the Ant Design Select component to be mocked 
 * so that we can simulate selecting items or query the options in the drop-down.
 */

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
            "name": "Appraisal",
            "attributes": [
                "INT"
            ]
        },
        {
            "name": "Athletics",
            "attributes": [
                "STR",
                "COR",
                "STA"
            ]
        },
        {
            "name": "Intimidation",
            "attributes": [
                "STR",
                "PRS"
            ]
        }
    ],
    bonusSkills: [
        {
            "name": "Appraisal",
            "attributes": [
                "INT"
            ]
        },
        {
            "name": "Athletics",
            "attributes": [
                "STR",
                "COR",
                "STA"
            ]
        },
        {
            "name": "History",
            "attributes": [
                "INT"
            ]
        },
        {
            "name": "Intimidation",
            "attributes": [
                "STR",
                "PRS"
            ]
        },
        {
            "name": "Religion",
            "attributes": [
                "INT"
            ]
        }
    ]
};

// Mock the Select Ant Design component
// Mock adapted from https://github.com/ant-design/ant-design/issues/21080
type SelectMockProps = {
    mode: string
    onChange: (value: string[]) => void
    options: (DefaultOptionType[] | undefined)
};

const SELECT_TEST_ID = 'select-test-id';

jest.mock('antd', () => {
    const antd = jest.requireActual('antd');
  
    const Select = (props: SelectMockProps) => {
      const multiple = ['tags', 'multiple'].includes(props.mode);
      let options = undefined;

      if (props.options) {
        options = props.options.map(o => {
            return (<option key={o.value} value={o.value ? o.value : ''}>{o.value}</option>);
        });
      }
  
      return (
        <select
          multiple={multiple}
          data-testid={SELECT_TEST_ID}
          onChange={(e) =>
            props.onChange(multiple
                ? Array.from(e.target.selectedOptions).map((option) => option.value)
                : Array.of(e.target.value))
          }
        >
        {options}
        </select>
      );
    };
  
    return { ...antd, Select };
});

test('correct bonus skills displayed for human species', () => {
    const expectedBonusSkills = ['Appraisal', 'Athletics', 'History', 'Intimidation', 'Religion'];

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

    let selectOptionValues = screen.getAllByRole("option").map(x => x.getAttribute('value'));
    expect(selectOptionValues).toEqual(expectedBonusSkills);
});

test.each([
    ["dwarf"],
    ["elf"],
    ["halfling"]
])('correct species and bonus skills displayed for %s species', (speciesStr) => {
    const expectedSpeciesSkills = ['Appraisal', 'Athletics', 'Intimidation'];
    const expectedBonusSkills = ['Appraisal', 'Athletics', 'History', 'Intimidation', 'Religion'];

    jest.spyOn(skills, 'useSkillsData').mockReturnValue({
        data: MOCK_SKILLS_DATA,
        error: false,
        isLoading: false
    });

    const speciesContext = {
        species: speciesStr
    };

    render(
        <CharacterContext.Provider value={speciesContext}>
            <Skills />
        </CharacterContext.Provider>
    );

    let radioButtonValues = screen.getAllByRole("radio").map(x => x.getAttribute('value'));
    let selectOptionValues = screen.getAllByRole("option").map(x => x.getAttribute('value'));

    expect(radioButtonValues).toEqual(expectedSpeciesSkills);
    expect(selectOptionValues).toEqual(expectedBonusSkills);
});

test('selecting 2 skills from select widget for humans sets context correctly and enables Next button', async () => {
    jest.spyOn(skills, 'useSkillsData').mockReturnValue({
        data: MOCK_SKILLS_DATA,
        error: false,
        isLoading: false
    });

    const newBonusSkills: string[] = [];
    const speciesContext = {
        species: 'human',
        bonusSkills: newBonusSkills,
        setBonusSkills: jest.fn()
    };
    speciesContext.setBonusSkills = jest.fn(skill => speciesContext.bonusSkills.push(skill[0]));

    const SKILL_1 = "History";
    const SKILL_2 = "Religion";

    const mockSetNextEnabled = jest.fn();
    const nextButtonEnabledContext = {
        setNextEnabled: mockSetNextEnabled
    };

    render(
        <CharacterContext.Provider value={speciesContext}>
        <NextButtonEnabledContext.Provider value={nextButtonEnabledContext}>
            <Skills />
        </NextButtonEnabledContext.Provider>
        </CharacterContext.Provider>
    );

    const selectElt = screen.getByTestId(SELECT_TEST_ID);
    act(() => {
        userEvent.selectOptions(selectElt, [SKILL_1, SKILL_2]);
    })

    // The next button should be disabled on initial render, and only enabled
    // after selecting 2 elements from the select dropdown
    expect(mockSetNextEnabled).toHaveBeenCalledTimes(3);
    expect(mockSetNextEnabled).toHaveBeenNthCalledWith(1, false);
    expect(mockSetNextEnabled).toHaveBeenLastCalledWith(true);

    expect(speciesContext.setBonusSkills).toHaveBeenCalledTimes(2); 
});

test.each([
    ["dwarf"],
    ["elf"],
    ["halfling"]
])('selecting species skill and bonus skill for non-human species %s sets context correctly and enables Next button', (speciesStr) => {
    jest.spyOn(skills, 'useSkillsData').mockReturnValue({
        data: MOCK_SKILLS_DATA,
        error: false,
        isLoading: false
    });

    const SPECIES_SKILL = 'Athletics (STR,COR,STA)';
    const SPECIES_SKILL_2 = "Intimidation (STR,PRS)";
    const BONUS_SKILL = 'History';

    const newBonusSkills: string[] = [];
    // The speciesSkill value needs to be hardcoded because it can't be changed after the initial context is created
    const speciesContext = {
        species: speciesStr,
        speciesSkill: SPECIES_SKILL,
        setSpeciesSkill: jest.fn(),
        bonusSkills: newBonusSkills,
        setBonusSkills: jest.fn()
    };
    speciesContext.setBonusSkills = jest.fn(skill => speciesContext.bonusSkills.push(skill[0]));

    const mockSetNextEnabled = jest.fn();
    const nextButtonEnabledContext = {
        setNextEnabled: mockSetNextEnabled
    };

    render(
        <CharacterContext.Provider value={speciesContext}>
        <NextButtonEnabledContext.Provider value={nextButtonEnabledContext}>
            <Skills />
        </NextButtonEnabledContext.Provider>
        </CharacterContext.Provider>
    );

    // If the button selected has the same value as the initial context,
    // the method to update the context never gets called
    const speciesSkillElt = screen.getByRole('radio', {name: SPECIES_SKILL_2});
    fireEvent.click(speciesSkillElt);

    const selectElt = screen.getByTestId(SELECT_TEST_ID);
    act(() => {
        userEvent.selectOptions(selectElt, [BONUS_SKILL]);
    })

    // The next button should be disabled on initial render, and only enabled
    // after selecting 1 element from the bonus skill selector and 1 species skill
    expect(mockSetNextEnabled).toHaveBeenCalledTimes(3);
    expect(mockSetNextEnabled).toHaveBeenNthCalledWith(1, false);
    expect(mockSetNextEnabled).toHaveBeenLastCalledWith(true);

    expect(speciesContext.setBonusSkills).toHaveBeenCalledTimes(1);
    expect(speciesContext.setSpeciesSkill).toHaveBeenCalledTimes(1); 
});

test('clicking on species skill radio button correctly removes skill from bonus skill list', () => {
    const expectedBonusSkills = ['Appraisal', 'History', 'Intimidation', 'Religion'];
    const SPECIES_SKILL = 'Athletics (STR,COR,STA)';

    jest.spyOn(skills, 'useSkillsData').mockReturnValue({
        data: MOCK_SKILLS_DATA,
        error: false,
        isLoading: false
    });

    const speciesContext = {
        species: 'dwarf',
        setSpeciesSkill: jest.fn()
    };

    render(
        <CharacterContext.Provider value={speciesContext}>
            <Skills />
        </CharacterContext.Provider>
    );

    const speciesSkillElt = screen.getByRole('radio', {name: SPECIES_SKILL});
    fireEvent.click(speciesSkillElt);

    let selectOptionValues = screen.getAllByRole("option").map(x => x.getAttribute('value'));
    expect(selectOptionValues).toEqual(expectedBonusSkills);
});

test('clicking on multiple species skill radio buttons correctly updates bonus skill list', () => {
    const expectedBonusSkills = ['Appraisal', 'Athletics', 'History', 'Religion'];
    const APPRAISAL_SKILL = 'Appraisal (INT)';
    const ATHLETICS_SKILL = 'Athletics (STR,COR,STA)';
    const INTIMIDATION_SKILL = 'Intimidation (STR,PRS)';

    jest.spyOn(skills, 'useSkillsData').mockReturnValue({
        data: MOCK_SKILLS_DATA,
        error: false,
        isLoading: false
    });

    const speciesContext = {
        species: 'dwarf',
        setSpeciesSkill: jest.fn()
    };

    render(
        <CharacterContext.Provider value={speciesContext}>
            <Skills />
        </CharacterContext.Provider>
    );

    let speciesSkillElt = screen.getByRole('radio', {name: ATHLETICS_SKILL});
    fireEvent.click(speciesSkillElt);

    speciesSkillElt = screen.getByRole('radio', {name: APPRAISAL_SKILL});
    fireEvent.click(speciesSkillElt);

    speciesSkillElt = screen.getByRole('radio', {name: INTIMIDATION_SKILL});
    fireEvent.click(speciesSkillElt);

    let selectOptionValues = screen.getAllByRole("option").map(x => x.getAttribute('value'));
    expect(selectOptionValues).toEqual(expectedBonusSkills);
});