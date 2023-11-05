import { Button, Steps } from "antd";
import { LeftOutlined, RightOutlined } from '@ant-design/icons';
import { ReactNode, useState } from "react";
import { NextButtonEnabledContext, CharacterContext } from "../Context";

type ChargenStepperProps = {
    steps: {title: string, content: ReactNode | undefined}[];
};

const ChargenStepper = (props: ChargenStepperProps) => {
    const [current, setCurrent] = useState(0);
    const [nextEnabled, setNextEnabled] = useState(false);
    const [species, setSpecies] = useState('');
    const [profession, setProfession] = useState('');
    const [charClass, setCharClass] = useState('');
    const [speciesSkill, setSpeciesSkill] = useState('');
    const [bonusSkills, setBonusSkills] = useState<string[]>([]);

    const nextEnabledValue = {nextEnabled, setNextEnabled};
    const characterInfoValue = {species, setSpecies,
        profession, setProfession,
        charClass, setCharClass,
        speciesSkill, setSpeciesSkill,
        bonusSkills, setBonusSkills};

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