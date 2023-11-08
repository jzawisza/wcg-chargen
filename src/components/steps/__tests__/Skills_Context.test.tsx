import { render, screen, act, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { DefaultOptionType } from "antd/es/select";
import Skills from "../Skills";
import { CharacterContext, NextButtonEnabledContext } from "../../../Context";

/**
 * Tests for the Skills component that validate setting of the React context.
 * 
 * These tests require the Ant Design Select component to be mocked, and are
 * therefore separate from the other tests of this component.
 */

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

test('selecting 2 skills from select widget for humans sets context correctly and enables Next button', async () => {
    const newBonusSkills: string[] = [];
    const speciesContext = {
        species: 'human',
        bonusSkills: newBonusSkills,
        setBonusSkills: jest.fn()
    };
    speciesContext.setBonusSkills = jest.fn(skill => speciesContext.bonusSkills.push(skill[0]));

    const SKILL_1 = "Skill 9";
    const SKILL_2 = "Skill 10";

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
    const SPECIES_SKILL = 'Skill 6';
    const SPECIES_SKILL_2 = "Skill 7";
    const BONUS_SKILL = 'Skill 9';

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