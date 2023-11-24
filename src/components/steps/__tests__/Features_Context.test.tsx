import { render, screen, act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { DefaultOptionType } from "antd/es/select";
import Features from "../Features";
import { CharacterContext, NextButtonEnabledContext } from "../../../Context";

/**
 * Tests for the Features component that validate setting of the React context.
 * 
 * These tests require the Ant Design Select component to be mocked, and are
 * therefore separate from the other tests of this component.
 */

// Mock the Select Ant Design component
type SelectMockProps = {
    mode: string
    onChange: (value: string[]) => void
    options: (DefaultOptionType[] | undefined)
};

const SELECT_TEST_ID_TIER_I = 'select-test-id-tier-1';
const SELECT_TEST_ID_TIER_II = 'select-test-id-tier-2';

jest.mock('antd', () => {
    const antd = jest.requireActual('antd');
  
    const Select = (props: SelectMockProps) => {
      const multiple = ['tags', 'multiple'].includes(props.mode);
      let options = undefined;

      let testId = SELECT_TEST_ID_TIER_I;
      if (props.options) {
        options = props.options.map(o => {
            return (<option key={o.value} value={o.value ? o.value : ''}>{o.value}</option>);
        });

        // Hack to set the test ID to the Tier II ID if any list items include "Tier II"
        if (props.options.find(x => x.value?.toString().includes("Tier II"))) {
            testId = SELECT_TEST_ID_TIER_II;
        }
      }
  
      return (
        <select
          multiple={multiple}
          data-testid={testId}
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

function getFeatureList(isTier2: boolean, numFeatures: number) {
    const tierName = isTier2 ? "Tier II" : "Tier I";

    let featureList = [];
    for (let i = 0; i < numFeatures; i++) {
        featureList.push(`${tierName} Feature ${i + 1}`);
    }

    return featureList;
}

test.each([
    [2, 1, 0],
    [3, 3, 0],
    [4, 3, 1],
    [5, 3, 2],
    [6, 4, 3],
    [7, 5, 4]
])('selecting approrpriate tier I/II features for level %d sets context correctly and enables Next button', async (charLevel, numTier1Features, numTier2Features) => {
    const emptyStringArray: string[] = [];
    const hasTier2Features = (numTier2Features > 0);

    const featuresContext = {
        level: charLevel,
        tier1Features: emptyStringArray,
        setTier1Features: jest.fn(),
        tier2Features: emptyStringArray,
        setTier2Features: jest.fn()
    };
    featuresContext.setTier1Features = jest.fn(feature => featuresContext.tier1Features.push(feature));
    featuresContext.setTier2Features = jest.fn(feature => featuresContext.tier2Features.push(feature));

    const mockSetNextEnabled = jest.fn();
    const nextButtonEnabledContext = {
        setNextEnabled: mockSetNextEnabled
    };

    render(
        <CharacterContext.Provider value={featuresContext}>
        <NextButtonEnabledContext.Provider value={nextButtonEnabledContext}>
            <Features />
        </NextButtonEnabledContext.Provider>
        </CharacterContext.Provider>
    );

    const tier1ElementsToSelect = getFeatureList(false, numTier1Features);
    const tier1SelectElt = screen.getByTestId(SELECT_TEST_ID_TIER_I);

    act(() => {
        userEvent.selectOptions(tier1SelectElt, tier1ElementsToSelect);
        if (hasTier2Features) {
            const tier2ElementsToSelect = getFeatureList(true, numTier2Features);
            const tier2SelectElt = screen.getByTestId(SELECT_TEST_ID_TIER_II);
            userEvent.selectOptions(tier2SelectElt, tier2ElementsToSelect);
        }
    });

    // The next button should be disabled on initial render, and only enabled
    // after selecting the appropriate number of elements from the select dropdown(s)
    expect(mockSetNextEnabled).toHaveBeenCalledTimes(1 + numTier1Features + numTier2Features);
    expect(mockSetNextEnabled).toHaveBeenNthCalledWith(1, false);
    expect(mockSetNextEnabled).toHaveBeenLastCalledWith(true);

    expect(featuresContext.setTier1Features).toHaveBeenCalledTimes(numTier1Features);
    expect(featuresContext.setTier2Features).toHaveBeenCalledTimes(numTier2Features);
});