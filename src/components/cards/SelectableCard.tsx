import { Card, Popover } from "antd";
import { InfoCircleOutlined } from '@ant-design/icons';
import { ReactNode } from "react";

type SelectableCardProps = {
    title: string,
    description: string,
    features: ReactNode,
    selected: boolean
};

// Card that will highlight when selected.
const SelectableCard = (props: SelectableCardProps) => {
    const classNameStr = "selectableCard" + (props.selected ? " selectableCard-selected" : "");

    return (
        <Card title={props.title}           
            extra={
                <Popover content={props.features} title="Features">
                    <InfoCircleOutlined />
                </Popover>
            }
            className={classNameStr}>
            <p>{props.description}</p>
        </Card>
    );
};

export default SelectableCard;