import React, { useEffect, useContext } from "react";
import { Radio, RadioChangeEvent, Spin } from "antd";
import useSWRImmutable from 'swr/immutable';
import { preload } from "swr";
import { CharacterContext, NextButtonEnabledContext } from "../../Context";
import fetcher from "../../Fetcher";

interface ProfessionType {
    name: string,
    rangeStart: number,
    rangeEnd: number
}

interface ProfessionsType {
    professions: ProfessionType[]
}

/**
 * Take profession data from the backend and parse it into radio buttons
 * that the user can select from.
 * 
 * @param data Profession data from the backend
 */
function generateRadioButtons(data: ProfessionsType | undefined) {
    if (!data) {
        return [];
    }

    // Get names of all returned professions
    const professionNames = data.professions.map(x => x.name).sort();
    // Map the names to radio buttons
    const radioButtons = professionNames.map(x => (<Radio.Button key={x} value={x}>{x}</Radio.Button>));

    return radioButtons;
}

preload('api/v1/professions/generate', fetcher);

const Profession: React.FC = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);
    const { profession, setProfession } = useContext(CharacterContext);
    const { data, error, isLoading } = useSWRImmutable<ProfessionsType>('api/v1/professions/generate', fetcher);
 
    useEffect(() => {
        setNextEnabled(profession !== '');
    }, [profession, setNextEnabled]);

    const onRadioGroupChange = (e: RadioChangeEvent) => {
        setProfession(e.target.value);
        setNextEnabled(true);
    };

    if (error) {
        return (
            <p>Error loading professions data from server.</p>
        );
    }
    if (isLoading) {
        return (
            <Spin size="large" />
        );
    }

    return (
        <div>
            <p>Commoner characters do not have any skills, but they do have a profession.  These are treated like trained skills, and may come in handy during your adventure.</p>
            <p>Select one of the professions from the buttons below.  These professions have been randomly selected for you.</p>
            <div className="professionCenter">
                <Radio.Group buttonStyle="solid" onChange={onRadioGroupChange} value={profession}>
                        {generateRadioButtons(data)}
                </Radio.Group>
            </div>
        </div>
    );
};

export default Profession;