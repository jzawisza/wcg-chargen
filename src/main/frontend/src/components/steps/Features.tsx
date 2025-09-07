import React, {useContext, useEffect, useState} from "react";
import { NextButtonEnabledContext, CharacterContext } from "../../Context";
import { Row, Col, Spin } from "antd";
import SelectMultiple from "../util/SelectMultiple";
import { useFeaturesData } from "../../server/ServerData";
import { Feature } from "../../server/FeaturesType";
import IncompleteFeatures from "../util/IncompleteFeatures";

const ATTR_PLUS_1_ATTRIBUTE = "ATTR_PLUS_1";
const SKILL_ATTRIBUTE = "SKILL";
const DOUBLE_ADVANTAGE_ATTRIBUTE = "DADV";
const ANY_MODIFIER = "Any";

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

/**
 * Process a list of features from the server, and return a list of feature names
 * that cannot be fully populated on the character sheet given the current functionality
 * of this application.
 * 
 * @param featureList List of features from the server
 * @returns A list of strings representing features that are not fully populated
 */
function getIncompleteFeatures(featureList: Feature[] | undefined) {
    if (!featureList) {
        return [];
    }

    const incompleteFeatures: string[] = [];
    featureList.forEach(f => {
        if (f.attributes && f.attributes.length > 0) {
            f.attributes.forEach(attr => {
                if (attr.type === ATTR_PLUS_1_ATTRIBUTE ||
                    attr.type === SKILL_ATTRIBUTE ||
                    (attr.type === DOUBLE_ADVANTAGE_ATTRIBUTE && attr.modifier === ANY_MODIFIER)) {
                        incompleteFeatures.push(f.description);
                    }
                });
        }
        });

    return incompleteFeatures;
}

const Features: React.FC = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);
    const { level, tier1Features, setTier1Features,
        tier2Features, setTier2Features, charClass } = useContext(CharacterContext);
    const { data, error, isLoading } = useFeaturesData(charClass, level);
    // Lists of incomplete features derived from the server response
    const [tier1IncompleteFeatures, setTier1IncompleteFeatures] = useState<string[]>([]);
    const [tier2IncompleteFeatures, setTier2IncompleteFeatures] = useState<string[]>([]);
    // Incomplete features that the user has selected
    const [tier1SelectedIncompleteFeatures, setTier1SelectedIncompleteFeatures] = useState<string[]>([]);
    const [tier2SelectedIncompleteFeatures, setTier2SelectedIncompleteFeatures] = useState<string[]>([]);

    const numTier1Features = data ? data.numAllowedTier1Features : 0;
    const numTier2Features = data ? data.numAllowedTier2Features : 0;
    const hasTier2Features = (numTier2Features > 0);
    const tier1FeaturesSelectDescriptor = `Select ${numTier1Features} feature${numTier1Features > 1 ? "s" : ""}`;
    const tier2FeaturesSelectDescriptor = `Select ${numTier2Features} feature${numTier2Features > 1 ? "s" : ""}`;
    const tier1FeaturesList = toDefaultOptionTypeList(data?.features?.tier1);
    const tier2FeaturesList = toDefaultOptionTypeList(data?.features?.tier2);
    const incompleteFeaturesSelected = tier1SelectedIncompleteFeatures.length > 0 || tier2SelectedIncompleteFeatures.length > 0; 

    useEffect(() => {
        let shouldEnableNext = getShouldEnableNext(tier1Features, tier2Features, numTier1Features, numTier2Features);
        setNextEnabled(shouldEnableNext);

        setTier1IncompleteFeatures(getIncompleteFeatures(data?.features?.tier1));
        setTier2IncompleteFeatures(getIncompleteFeatures(data?.features?.tier2)); 
    }, [data, tier1Features, tier2Features, numTier1Features, numTier2Features, setNextEnabled]);

    const onTier1FeatureChange = (value: string[]) => {
        setTier1Features(value);

        const arrayValue = Array.isArray(value) ? value : [value];
        const newTier1SelectedIncompleteFeatures: string[] = [];
        arrayValue.forEach(v => {
            if (tier1IncompleteFeatures.includes(v)) {
                newTier1SelectedIncompleteFeatures.push(v);
            }
        });
        setTier1SelectedIncompleteFeatures(newTier1SelectedIncompleteFeatures);

        let shouldEnableNext = getShouldEnableNext(value, tier2Features, numTier1Features, numTier2Features);
        setNextEnabled(shouldEnableNext);
    };

    const onTier2FeatureChange = (value: string[]) => {
        setTier2Features(value);
        
        const arrayValue = Array.isArray(value) ? value : [value];
        const newTier2SelectedIncompleteFeatures: string[] = [];
        arrayValue.forEach(v => {
            if (tier2IncompleteFeatures.includes(v)) {
                newTier2SelectedIncompleteFeatures.push(v);
            }
        });
        setTier2SelectedIncompleteFeatures(newTier2SelectedIncompleteFeatures);

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

            {incompleteFeaturesSelected &&
            <IncompleteFeatures
                tier1Features={tier1SelectedIncompleteFeatures}
                tier2Features={tier2SelectedIncompleteFeatures}
            />}

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