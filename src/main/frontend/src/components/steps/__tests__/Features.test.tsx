import { render, screen } from "@testing-library/react";
import Features from "../Features";
import { CharacterContext } from "../../../Context";
import * as features from "../../../server/ServerData";

const MOCK_EMPTY_SKILLS_DATA = {
    numAllowedTier1Features: 0,
    numAllowedTier2Features: 0,
    features: {
        tier1: [],
        tier2: []
    }
};

function getFeaturesData(tier1Features: number, tier2Features: number) {
    return {
        numAllowedTier1Features: tier1Features,
        numAllowedTier2Features: tier2Features,
        features: {
            tier1: [],
            tier2: []
        }
    };
}
function getExpectedSelectorText(numFeatures: number) {
    if (numFeatures === 0) {
        return "";
    }
    
    return (numFeatures === 1) ? "Select 1 feature" : `Select ${numFeatures} features`;
}

function getSelectorTextUnderHeading(headingName: string) {
    return screen.getByText(headingName).nextElementSibling?.firstChild?.firstChild?.nextSibling?.textContent;
}


test('tier I features displayed by default', () => {
    var featuresData = getFeaturesData(1, 0);
    jest.spyOn(features, 'useFeaturesData').mockReturnValue({
        data: featuresData,
        error: false,
        isLoading: false
    });

    const levelContext = {
        level: 2
    };

    render(
        <CharacterContext.Provider value={levelContext}>
            <Features />
        </CharacterContext.Provider>
    );

    const tier1FeaturesHeading = screen.getByText('Tier I Features');
    expect(tier1FeaturesHeading).toBeTruthy();
});

test.each([
    [0, false],
    [1, true]
])('tier II features only displayed if number of tier II features from server is greater than 0 (should display for %d features = %s)', (numTier2Features, shouldDisplayTier2Features) => {
    var featuresData = getFeaturesData(1, numTier2Features);
    jest.spyOn(features, 'useFeaturesData').mockReturnValue({
        data: featuresData,
        error: false,
        isLoading: false
    });

    const levelContext = {
        level: 4
    };

    render(
        <CharacterContext.Provider value={levelContext}>
            <Features />
        </CharacterContext.Provider>
    );

    const tier2FeaturesHeading = screen.queryByText('Tier II Features');
    if (!shouldDisplayTier2Features) {
        expect(tier2FeaturesHeading).toBeNull();
    }
    else {
        expect(tier2FeaturesHeading).toBeTruthy();
    }
});

test('correct number of tier I and tier II features selectable based on server information', () => {
    const expectedNumTier1Features = 3;
    const expectedNumTier2Features = 2;
    var featuresData = getFeaturesData(expectedNumTier1Features, expectedNumTier2Features);

    jest.spyOn(features, 'useFeaturesData').mockReturnValue({
        data: featuresData,
        error: false,
        isLoading: false
    });

    const levelContext = {
        level: 5
    };

    const expectedTier1Text = getExpectedSelectorText(expectedNumTier1Features);
    const expectedTier2Text = getExpectedSelectorText(expectedNumTier2Features);

    render(
        <CharacterContext.Provider value={levelContext}>
            <Features />
        </CharacterContext.Provider>
    );

    const tier1FeatureSelectionText = getSelectorTextUnderHeading('Tier I Features');    
    expect(tier1FeatureSelectionText).toEqual(expectedTier1Text);

    const tier2FeatureSelectionText = getSelectorTextUnderHeading('Tier II Features');    
    expect(tier2FeatureSelectionText).toEqual(expectedTier2Text);
});

test('error from server displays expected output', () => {
    jest.spyOn(features, 'useFeaturesData').mockReturnValue({
        data: MOCK_EMPTY_SKILLS_DATA,
        error: true,
        isLoading: false
    });

    render(<Features />);

    const errorText = screen.getByText('Error loading features data from server.');

    expect(errorText).toBeTruthy();
});

test('data loading from server displays expected output', () => {
    jest.spyOn(features, 'useFeaturesData').mockReturnValue({
        data: MOCK_EMPTY_SKILLS_DATA,
        error: false,
        isLoading: true
    });

    const { container } = render(<Features />);

    const spinner = container.querySelector('.ant-spin');

    expect(spinner).toBeTruthy();
});