import React, {useContext, useEffect} from "react";
import { NextButtonEnabledContext, CharacterContext } from "../../Context";
import { Row, Col } from "antd";
import SelectMultiple from "../util/SelectMultiple";
import { DefaultOptionType } from "antd/es/select";

// This list is cumulative, not per level.  For example, a Level 3 character would have
// 3 Tier I features in total, 1 from Level 2 and 2 from Level 3.
const totalTier1And2FeaturesPerLevel = [
    [0, 0],
    [0, 0],
    [1, 0], // Level 2
    [3, 0], // Level 3
    [3, 1], // Level 4
    [3, 2], // Level 5
    [4, 3], // Level 6
    [5, 4], // Level 7
];

const dummyTier1Features: DefaultOptionType[] = [
    { value: 'Tier I Feature 1', label: 'Tier I Feature 1' },
    { value: 'Tier I Feature 2', label: 'Tier I Feature 2' },
    { value: 'Tier I Feature 3', label: 'Tier I Feature 3' },
    { value: 'Tier I Feature 4', label: 'Tier I Feature 4' },
    { value: 'Tier I Feature 5', label: 'Tier I Feature 5' },
    { value: 'Tier I Feature 6', label: 'Tier I Feature 6' }
];

const dummyTier2Features: DefaultOptionType[] = [
    { value: 'Tier II Feature 1', label: 'Tier II Feature 1' },
    { value: 'Tier II Feature 2', label: 'Tier II Feature 2' },
    { value: 'Tier II Feature 3', label: 'Tier II Feature 3' },
    { value: 'Tier II Feature 4', label: 'Tier II Feature 4' },
    { value: 'Tier II Feature 5', label: 'Tier II Feature 5' },
    { value: 'Tier II Feature 6', label: 'Tier II Feature 6' }
];

function getShouldEnableNext(tier1Features: string[], tier2Features: string[],
    numTier1Features: number, numTier2Features: number) {
    if (!tier1Features || !tier2Features) {
        return false;
    }

    return (tier1Features.length >= numTier1Features && tier2Features.length >= numTier2Features);
}

const Features: React.FC = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);
    const { level, tier1Features, setTier1Features, tier2Features, setTier2Features } = useContext(CharacterContext);

    const hasTier2Features = (level >= 4);
    const numTier1Features = totalTier1And2FeaturesPerLevel[level][0];
    const numTier2Features = totalTier1And2FeaturesPerLevel[level][1];
    const tier1FeaturesSelectDescriptor = `Select ${numTier1Features} feature${numTier1Features > 1 ? "s" : ""}`;
    const tier2FeaturesSelectDescriptor = `Select ${numTier2Features} feature${numTier2Features > 1 ? "s" : ""}`;

    useEffect(() => {
        let shouldEnableNext = getShouldEnableNext(tier1Features, tier2Features, numTier1Features, numTier2Features);
        setNextEnabled(shouldEnableNext);
    }, [tier1Features, tier2Features, numTier1Features, numTier2Features, setNextEnabled]);

    const onTier1FeatureChange = (value: string[]) => {
        setTier1Features(value);

        let shouldEnableNext = getShouldEnableNext(value, tier2Features, numTier1Features, numTier2Features);
        setNextEnabled(shouldEnableNext);
    };

    const onTier2FeatureChange = (value: string[]) => {
        setTier2Features(value);

        let shouldEnableNext = getShouldEnableNext(tier1Features, value, numTier1Features, numTier2Features);
        setNextEnabled(shouldEnableNext);
    };

    return (
        <div>
            <p>Characters Level 2 and above can select advanced features that are gained upon levelling up.  These features allow you to customize your character's abilities and talents.</p>
            <p>Features are divided into two tiers, Tier I and Tier II.  Tier II features are more powerful, and are available to characters Level 4 and above.</p>
            <p>Select your desired features from the {hasTier2Features? "lists" : "list"} below.</p>

            <Row justify="center">
                <Col span={6}>
                    <div>
                        <h3>Tier I Features</h3>
                        <SelectMultiple
                            defaultValue={tier1Features}
                            numElementsAllowed={numTier1Features}
                            onChange={onTier1FeatureChange}
                            options={dummyTier1Features}
                            placeholder={tier1FeaturesSelectDescriptor}
                        />
                    </div>
                </Col>
                {hasTier2Features && (
                    <Col span={6}>
                        <div>
                            <h3>Tier II Features</h3>
                            <SelectMultiple
                                defaultValue={tier2Features}
                                numElementsAllowed={numTier2Features}
                                onChange={onTier2FeatureChange}
                                options={dummyTier2Features}
                                placeholder={tier2FeaturesSelectDescriptor}
                            />
                        </div>
                    </Col>
                )}
            </Row>
        </div>
    );
};

export default Features;