import { render, screen, act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { DefaultOptionType } from "antd/es/select";
import Features from "../Features";
import { CharacterContext, NextButtonEnabledContext } from "../../../Context";
import * as features from "../../../server/ServerData";
import exp from "constants";

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

const NUM_ALLOWED_TIER_1_FEATURES = 5;
const NUM_ALLOWED_TIER_2_FEATURES = 3;
const MOCK_FEATURES_DATA = {
    numAllowedTier1Features: NUM_ALLOWED_TIER_1_FEATURES,
    numAllowedTier2Features: NUM_ALLOWED_TIER_2_FEATURES,
    features: {
        tier1: [
            { description: "Tier I Feature 1", attributes: [] },
            { description: "Tier I Feature 2", attributes: [] },
            { description: "Tier I Feature 3", attributes: [] },
            { description: "Tier I Feature 4", attributes: [] },
            { description: "Tier I Feature 5", attributes: [] }
        ],
        tier2: [
            { description: "Tier II Feature 1", attributes: [] },
            { description: "Tier II Feature 2", attributes: [] },
            { description: "Tier II Feature 3", attributes: [] }
        ]
    }
};

test('selecting appropriate number of tier I/II features sets context correctly and enables Next button', async () => {
    const emptyStringArray: string[] = [];

    jest.spyOn(features, 'useFeaturesData').mockReturnValue({
        data: MOCK_FEATURES_DATA,
        error: false,
        isLoading: false
    });

    const featuresContext = {
        level: 6,
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

    const tier1ElementsToSelect = getFeatureList(false, NUM_ALLOWED_TIER_1_FEATURES);
    const tier1SelectElt = screen.getByTestId(SELECT_TEST_ID_TIER_I);
    const tier2ElementsToSelect = getFeatureList(true, NUM_ALLOWED_TIER_2_FEATURES);
    const tier2SelectElt = screen.getByTestId(SELECT_TEST_ID_TIER_II);

    act(() => {
        userEvent.selectOptions(tier1SelectElt, tier1ElementsToSelect);
        userEvent.selectOptions(tier2SelectElt, tier2ElementsToSelect);
    });

    // The next button should be disabled on initial render, and only enabled
    // after selecting the appropriate number of elements from the select dropdown(s)
    expect(mockSetNextEnabled).toHaveBeenCalledTimes(1 + NUM_ALLOWED_TIER_1_FEATURES + NUM_ALLOWED_TIER_2_FEATURES);
    expect(mockSetNextEnabled).toHaveBeenNthCalledWith(1, false);
    expect(mockSetNextEnabled).toHaveBeenLastCalledWith(true);

    expect(featuresContext.setTier1Features).toHaveBeenCalledTimes(NUM_ALLOWED_TIER_1_FEATURES);
    expect(featuresContext.setTier2Features).toHaveBeenCalledTimes(NUM_ALLOWED_TIER_2_FEATURES);
});

test.each([
    ['ATTR_PLUS_1', '', true],
    ['SKILL', '', true],
    ['DADV', 'Any', true],
    ['ATTR_PLUS_1', '', false],
    ['SKILL', '', false],
    ['DADV', 'Any', false]
])('tier I/II incomplete features are displayed when selected', async (attrType, attrModifier, isTier1) => {
    const emptyStringArray: string[] = [];
    const tierFeatureName = isTier1 ? "Tier I" : "Tier II";
    const incompleteFeatureName = `Incomplete Feature ${tierFeatureName}`;
    const featuresArray = [
        { description: "Unselected Feature", attributes: [] },
        { description: "Complete Feature", attributes: [] },
        { description: incompleteFeatureName, attributes: [ {type: attrType, modifier: attrModifier}] }
    ];
    const mockFeaturesData = {
        numAllowedTier1Features: isTier1 ? 2 : 0,
        numAllowedTier2Features: isTier1 ? 0 : 2,
        features: {
            tier1: isTier1 ? featuresArray : [],
            tier2: isTier1 ? [] : featuresArray
        }
    };

    jest.spyOn(features, 'useFeaturesData').mockReturnValue({
        data: mockFeaturesData,
        error: false,
        isLoading: false
    });

    const featuresContext = {
        level: 6,
        tier1Features: emptyStringArray,
        setTier1Features: jest.fn(),
        tier2Features: emptyStringArray,
        setTier2Features: jest.fn()
    };

    render(
        <CharacterContext.Provider value={featuresContext}>
            <Features />  
        </CharacterContext.Provider>
    );

    const elementsToSelect = [incompleteFeatureName, "Complete Feature"];
    const testId = isTier1 ? SELECT_TEST_ID_TIER_I : SELECT_TEST_ID_TIER_II;
    const featuresSelectElt = screen.getByTestId(testId);

    act(() => {
        userEvent.selectOptions(featuresSelectElt, elementsToSelect);
    });

    const incompleteFeatureTextElt = screen.getByText("The following features will not be automatically populated on your character sheet, and must be applied by hand after the character sheet is generated.");
    const featureHeaderElt = screen.getByText(isTier1 ? "Tier I Features:" : "Tier II Features:");
    const missingFeatureHeaderElt = screen.queryByText(isTier1 ? "Tier II Features:" : "Tier I Features:");
    const incompleteFeatureElt = screen.getByText(incompleteFeatureName, { selector: 'li' });
    const completeFeatureElt = screen.queryByText('Complete Feature', { selector: 'li' });
    const unselectedFeatureElt = screen.queryByText('Unselected Feature', { selector: 'li' });

    expect(incompleteFeatureTextElt).toBeInTheDocument();
    expect(featureHeaderElt).toBeInTheDocument();
    expect(missingFeatureHeaderElt).not.toBeInTheDocument();
    expect(incompleteFeatureElt).toBeInTheDocument();
    expect(completeFeatureElt).not.toBeInTheDocument();
    expect(unselectedFeatureElt).not.toBeInTheDocument();
});

test.each([
    [true],
    [false]
])('selecting incomplete feature, un-selecting it, and then re-selecting it shows one copy of incomplete feature', async (isTier1) => {
    const incompleteFeatureName = `Incomplete Feature ${isTier1 ? "Tier I" : "Tier II"}`;
    const completeFeatureName = `Complete Feature`;

    const emptyStringArray: string[] = [];
    const tierFeatureName = isTier1 ? "Tier I" : "Tier II";
    const featuresArray = [
        { description: "Unselected Feature", attributes: [] },
        { description: "Complete Feature", attributes: [] },
        { description: incompleteFeatureName, attributes: [ {type: 'ATTR_PLUS_1', modifier: ''}] }
    ];
    const mockFeaturesData = {
        numAllowedTier1Features: isTier1 ? 2 : 0,
        numAllowedTier2Features: isTier1 ? 0 : 2,
        features: {
            tier1: isTier1 ? featuresArray : [],
            tier2: isTier1 ? [] : featuresArray
        }
    };

    jest.spyOn(features, 'useFeaturesData').mockReturnValue({
        data: mockFeaturesData,
        error: false,
        isLoading: false
    });

    const featuresContext = {
        level: 6,
        tier1Features: emptyStringArray,
        setTier1Features: jest.fn(),
        tier2Features: emptyStringArray,
        setTier2Features: jest.fn()
    };

    const { container } = render(
        <CharacterContext.Provider value={featuresContext}>
            <Features />
        </CharacterContext.Provider>
    );

    const elementsToSelect = [incompleteFeatureName, completeFeatureName];
    const testId = isTier1 ? SELECT_TEST_ID_TIER_I : SELECT_TEST_ID_TIER_II;
    const featuresSelectElt = screen.getByTestId(testId);

    act(() => {
        userEvent.selectOptions(featuresSelectElt, elementsToSelect);
    });

    act(() => {
        userEvent.deselectOptions(featuresSelectElt, [incompleteFeatureName]);
    });

    act(() => {
        userEvent.selectOptions(featuresSelectElt, [incompleteFeatureName]);
    });

    const incompleteFeatureElt = screen.getByText(incompleteFeatureName, { selector: 'li' });
    const completeFeatureElt = screen.queryByText(completeFeatureName, { selector: 'li' });

    expect(incompleteFeatureElt).toBeInTheDocument();
    expect(completeFeatureElt).not.toBeInTheDocument();
});