import { render, screen } from "@testing-library/react";
import Features from "../Features";
import { CharacterContext } from "../../../Context";

function getExpectedSelectorText(numFeatures: number) {
    if (numFeatures === 0) {
        return "";
    }
    
    return (numFeatures === 1) ? "Select 1 feature" : `Select ${numFeatures} features`;
}

function getSelectorTextUnderHeading(headingName: string) {
    return screen.getByText(headingName).nextElementSibling?.firstChild?.firstChild?.nextSibling?.textContent;
}

test.each([
    [2],
    [3],
    [4],
    [5],
    [6],
    [7]
])('tier I features displayed for level %d', (charLevel) => {
    const levelContext = {
        level: charLevel
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
    [2, false],
    [3, false],
    [4, true],
    [5, true],
    [6, true],
    [7,true]
])('tier II features only displayed for levels 4 and above (level %d)', (charLevel, shouldDisplayTier2Features) => {
    const levelContext = {
        level: charLevel
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

test.each([
    [2, 1, 0],
    [3, 3, 0],
    [4, 3, 1],
    [5, 3, 2],
    [6, 4, 3],
    [7, 5, 4]
])('correct number of tier I and tier II features selectable for level %d', (charLevel, numTier1Features, numTier2Features) => {
    const levelContext = {
        level: charLevel
    };

    const expectedTier1Text = getExpectedSelectorText(numTier1Features);
    const expectedTier2Text = getExpectedSelectorText(numTier2Features);

    render(
        <CharacterContext.Provider value={levelContext}>
            <Features />
        </CharacterContext.Provider>
    );

    const tier1FeatureSelectionText = getSelectorTextUnderHeading('Tier I Features');    
    expect(tier1FeatureSelectionText).toEqual(expectedTier1Text);

    if (numTier2Features > 0) {
        const tier2FeatureSelectionText = getSelectorTextUnderHeading('Tier II Features');    
        expect(tier2FeatureSelectionText).toEqual(expectedTier2Text);
    }
});