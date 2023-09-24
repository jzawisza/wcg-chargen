import { Button, StepProps, Steps } from "antd";
import { LeftOutlined, RightOutlined } from '@ant-design/icons';
import { useState } from "react";

type ChargenStepperProps = {
    steps: StepProps[] | undefined;
};

const ChargenStepper = (props: ChargenStepperProps) => {
    const [current, setCurrent] = useState(0);
    const numSteps = props.steps ? props.steps.length : 0;

    return (
        <div>
            <Steps current={current} items={props.steps} />
            {current > 0 && (
                <Button type="text" className="prevButton" onClick={() => setCurrent(current - 1)} icon={<LeftOutlined />}>
                    Previous
                </Button>
            )}
            {current < (numSteps - 1) && (
                <Button type="text" className="nextButton" onClick={() => setCurrent(current + 1)} >
                    Next
                <RightOutlined />
            </Button>
            )}
        </div>
    );
};

export default ChargenStepper;