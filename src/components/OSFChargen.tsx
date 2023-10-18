import React, { useState } from "react";
import wcg from '../images/wcg_logo.png';
import { DownOutlined } from '@ant-design/icons';
import { Col, Row, Dropdown, Space } from "antd";
import type { MenuProps } from 'antd';
import ModeCard from "./ModeCard";
import ChargenStepper from "./ChargenStepper";
import Species from "./steps/Species";
import CreateCharacter from "./steps/CreateCharacter";

const FEATURES_STEP_TITLE: string = "Features";

const items: MenuProps['items'] = [
    {
        label: 1,
        key: 1
    },
    {
        label: 2,
        key: 2
    },
    {
        label: 3,
        key: 3
    },
    {
        label: 4,
        key: 4
    },
    {
        label: 5,
        key: 5
    },
    {
        label: 6,
        key: 6
    },
    {
        label: 7,
        key: 7
    },
];

const wickedHardSteps = [
    {
        title: 'Species',
        content: <Species />
    },
    {
        title: 'Profession',
        content: <div />
    },
    {
        title: 'Attributes',
        content: <div />
    },
    {
        title: 'Create Character',
        content: <CreateCharacter />
    }
];

const traditionalSteps = [
    {
        title: 'Species',
        content: <Species />
    },
    {
        title: 'Class',
        content: <div />
    },
    {
        title: 'Skills',
        content: <div />
    },
    {
        title: 'Attributes',
        content: <div />
    },
    {
        title: FEATURES_STEP_TITLE,
        content: <div />
    },
    {
        title: 'Create Character',
        content: <CreateCharacter />
    }
];

const OSFChargen: React.FC = () => {
    const [level, setLevel] = useState<number | null>(null);
    const [traditionalStepsForLevel, setTraditionalStepsForLevel] = useState(traditionalSteps);

    const onClick: MenuProps['onClick'] = ({ key }) => {
        let charLevel = parseInt(key);

        // Level 1 characters don't select features
        if (charLevel === 1) {
            setTraditionalStepsForLevel(traditionalSteps.filter((step) => step.title !== FEATURES_STEP_TITLE));
        }

        setLevel(charLevel);
      };

    // Base case: initial screen
    if (level == null)
    {
        return (
            <div className="padded">
                <img src={wcg} alt="Wicked Cool Games logo" className="imgCenter"/>
                <p>This is a character generator for the OSF (Old School Fantasy) RPG system to be published by Wicked Cool Games.</p>
                <p>Before creating a character, consult with your GM for guidance about what settings to use.</p>
                <p>Choose one of the options below to get started.</p>
                <Row justify="center">
                    <Col span={12}>
                        <div>
                            <ModeCard title="Traditional Start" description="Create a Level 1-7 character with a specific character class.">
                                <Dropdown menu={{ items, onClick }}>
                                    <a onClick={(e) => e.preventDefault()}>
                                        <Space>
                                            Level
                                            <DownOutlined />
                                        </Space>
                                    </a>
                                </Dropdown>
                            </ModeCard>
                        </div>
                    </Col>
                    <div onClick={() => setLevel(0)}>
                        <Col span={12}>
                            <ModeCard title="Wicked Hard Mode"
                                description="Make Level 0 unskilled commoner characters.  Those lucky enough to survive will be promoted to 1st level."></ModeCard>                    
                        </Col>
                    </div>
                </Row>
            </div>
        );
    }
    // Wicked Hard Mode stepper
    else if (level === 0) {
        return (
            <ChargenStepper steps={wickedHardSteps} />
        );
    }
    // Traditional Mode stepper
    else
    {
        return (
            <ChargenStepper steps={traditionalStepsForLevel} />
        );
    }
}

export default OSFChargen;