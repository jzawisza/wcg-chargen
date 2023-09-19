import React, { useState } from "react";
import wcg from '../images/wcg_logo.png';
import { DownOutlined } from '@ant-design/icons';
import { Col, Row, Dropdown, Space, Steps } from "antd";
import type { MenuProps, StepProps } from 'antd';
import ModeCard from "./ModeCard";

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
        title: 'Species'
    },
    {
        title: 'Profession'
    },
    {
        title: 'Attributes'
    },
    {
        title: 'Create Character'
    }
];

const traditionalSteps = [
    {
        title: 'Species'
    },
    {
        title: 'Class'
    },
    {
        title: 'Skills'
    },
    {
        title: 'Attributes'
    },
    {
        title: FEATURES_STEP_TITLE
    },
    {
        title: 'Create Character'
    }
];

export const OSFChargen: React.FC = () => {
    const [level, setLevel] = useState<number | null>(null);
    const [traditionalStepsForLevel, setTraditionalStepsForLevel] = useState<StepProps[] | undefined>(traditionalSteps);

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
            <Steps current={0} items={wickedHardSteps} />
        );
    }
    // Traditional Mode stepper
    else
    {
        return (
            <Steps current={0} items={traditionalStepsForLevel} />
        );
    }
}

export default OSFChargen;