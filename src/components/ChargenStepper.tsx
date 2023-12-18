import { Button, Steps } from "antd";
import { LeftOutlined, RightOutlined } from '@ant-design/icons';
import { ReactNode, useState } from "react";
import { NextButtonEnabledContext, CharacterContext } from "../Context";
import { emptyAtributeScoreObj } from "../constants/AttributeScoreObject";

type ChargenStepperProps = {
    steps: {title: string, content: ReactNode | undefined}[];
    level: number;
};

const ChargenStepper = (props: ChargenStepperProps) => {
    const level = props.level;
    const [current, setCurrent] = useState(0);
    const [nextEnabled, setNextEnabled] = useState(false);
    const [species, setSpecies] = useState('');
    const [profession, setProfession] = useState('');
    const [charClass, setCharClass] = useState('');
    const [speciesSkill, setSpeciesSkill] = useState('');
    const [bonusSkills, setBonusSkills] = useState<string[]>([]);
    const [tier1Features, setTier1Features] = useState<string[]>([]);
    const [tier2Features, setTier2Features] = useState<string[]>([]);
    const [charName, setCharName] = useState('');
    const [attributeArrayType, setAttributeArrayType] = useState('');
    const [attributeScoreObj, setAttributeScoreObj] = useState(emptyAtributeScoreObj);
    const [attributeValues, setAttributeValues] = useState<(number | null)[]>([]);

    const nextEnabledValue = {nextEnabled, setNextEnabled};
    const characterInfoValue = {level,
        species, setSpecies,
        profession, setProfession,
        charClass, setCharClass,
        speciesSkill, setSpeciesSkill,
        bonusSkills, setBonusSkills,
        tier1Features, setTier1Features,
        tier2Features, setTier2Features,
        charName, setCharName,
        attributeArrayType, setAttributeArrayType,
        attributeScoreObj, setAttributeScoreObj,
        attributeValues, setAttributeValues};

    const numSteps = props.steps ? props.steps.length : 0;

    return (
        <div>
            <NextButtonEnabledContext.Provider value={nextEnabledValue}>
            <CharacterContext.Provider value={characterInfoValue}>
                <Steps current={current} items={props.steps} />
                <div>{props.steps[current].content}</div>
                <hr />
                {current > 0 && (
                    <Button type="text" className="prevButton" onClick={() => setCurrent(current - 1)} icon={<LeftOutlined />}>
                        Previous
                    </Button>
                )}
                {current < (numSteps - 1) && (
                    <Button type="text" disabled={!nextEnabled} className="nextButton" onClick={() => setCurrent(current + 1)} >
                        Next
                    <RightOutlined />
                </Button>
                )}
            </CharacterContext.Provider>
            </NextButtonEnabledContext.Provider>
        </div>
    );
};

export default ChargenStepper;