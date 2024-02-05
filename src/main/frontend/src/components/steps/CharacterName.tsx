import React, {useContext, useEffect} from "react";
import { Input, Space } from "antd";
import { CharacterContext, NextButtonEnabledContext } from "../../Context";

const CharacterName: React.FC = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);
    const { charName, setCharName } = useContext(CharacterContext);

    const onInputCharacterName = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newCharName = e.target.value;
        setCharName(newCharName);

        setNextEnabled(newCharName !== '');
    };

    useEffect(() => {
        if (charName === '') {
            setNextEnabled(false);
        }
    }, [charName, setNextEnabled]);

    return (
        <div>
            <p>Enter a name for your character, and click Next to continue.</p>
            <p>There are many possible sources for character names, from real-world cultures past and present to simple puns to whatever your imagination might come up with.  Certain settings may impose restrictions on character names: your GM should have informed you of any such restrictions.</p>
            <Space>
                <b>Character name:</b>
                <Input onChange={onInputCharacterName}/>
            </Space>

        </div>
    )
};

export default CharacterName;