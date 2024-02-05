import React, { useEffect, useContext } from "react";
import { Radio, RadioChangeEvent } from "antd";
import { CharacterContext, NextButtonEnabledContext } from "../../Context";

const Profession: React.FC = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);
    const { profession, setProfession } = useContext(CharacterContext);

    useEffect(() => {
        setNextEnabled(profession !== '');
    }, [profession, setNextEnabled]);

    const onRadioGroupChange = (e: RadioChangeEvent) => {
        setProfession(e.target.value);
        setNextEnabled(true);
    };

    // TODO: get data dynamically from server instead of hardcoding it
    return (
        <div>
            <p>Commoner characters do not have any skills, but they do have a profession.  These are treated like trained skills, and may come in handy during your adventure.</p>
            <p>Select one of the professions from the buttons below.  These professions have been randomly selected for you.</p>
            <div className="professionCenter">
                <Radio.Group buttonStyle="solid" onChange={onRadioGroupChange} value={profession}>
                        <Radio.Button value='scribe'>Scribe</Radio.Button>
                        <Radio.Button value='shepherd'>Shepherd</Radio.Button>
                        <Radio.Button value='shipwright'>Shipwright</Radio.Button>
                </Radio.Group>
            </div>
        </div>
    );
};

export default Profession;