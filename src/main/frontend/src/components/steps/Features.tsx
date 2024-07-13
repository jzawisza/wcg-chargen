import React, {useContext, useEffect} from "react";
import { NextButtonEnabledContext, CharacterContext } from "../../Context";
import { Row, Col, Spin } from "antd";
import SelectMultiple from "../util/SelectMultiple";
import { useFeaturesData } from "../../server/ServerData";
import { Feature } from "../../server/FeaturesType";

function toDefaultOptionTypeList(featureList: Feature[] | undefined) {
    if (!featureList) {
        return [];
    }

    return featureList.map(f => ({ value: f.description, label: f.description }));
}

function getShouldEnableNext(tier1Features: string[], tier2Features: string[],
    numTier1Features: number, numTier2Features: number) {
    if (!tier1Features || !tier2Features) {
        return false;
    }

    return (tier1Features.length >= numTier1Features && tier2Features.length >= numTier2Features);
}

const Features: React.FC = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);
    const { level, tier1Features, setTier1Features,
        tier2Features, setTier2Features, charClass } = useContext(CharacterContext);
    const { data, error, isLoading } = useFeaturesData(charClass, level);

    const numTier1Features = data ? data.numAllowedTier1Features : 0;
    const numTier2Features = data ? data.numAllowedTier2Features : 0;
    const hasTier2Features = (numTier2Features > 0);
    const tier1FeaturesSelectDescriptor = `Select ${numTier1Features} feature${numTier1Features > 1 ? "s" : ""}`;
    const tier2FeaturesSelectDescriptor = `Select ${numTier2Features} feature${numTier2Features > 1 ? "s" : ""}`;
    const tier1FeaturesList = toDefaultOptionTypeList(data?.features?.tier1);
    const tier2FeaturesList = toDefaultOptionTypeList(data?.features?.tier2);

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

    if (error) {
        return (
            <p>Error loading features data from server.</p>
        );
    }
    if (isLoading) {
        return (
            <Spin size="large" />
        );
    }
    
    return (
        
        <div>
            <p>Characters Level 2 and above can select advanced features that are gained upon levelling up.  These features allow you to customize your character's abilities and talents.</p>
            <p>Features are divided into two tiers, Tier I and Tier II.  Tier II features are more powerful, and are available to characters Level 4 and above.</p>
            <p>Select your desired features from the {hasTier2Features? "lists" : "list"} below.</p>

            <Row justify="center">
                <Col span={12}>
                    <div>
                        <h3>Tier I Features</h3>
                        <SelectMultiple
                            defaultValue={tier1Features}
                            extraWide={true}
                            numElementsAllowed={numTier1Features}
                            onChange={onTier1FeatureChange}
                            options={tier1FeaturesList}
                            placeholder={tier1FeaturesSelectDescriptor}
                        />
                    </div>
                </Col>
                {hasTier2Features && (
                    <Col span={12}>
                        <div>
                            <h3>Tier II Features</h3>
                            <SelectMultiple
                                defaultValue={tier2Features}
                                extraWide={true}
                                numElementsAllowed={numTier2Features}
                                onChange={onTier2FeatureChange}
                                options={tier2FeaturesList}
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