import { Card } from "antd";
import { PropsWithChildren } from 'react';

type ModeCardProps = {
    title: string,
    description: string
};

const ModeCard = (props: PropsWithChildren<ModeCardProps>) => (
    <Card title={props.title}
        hoverable
        className="modeCard">
        <p>{props.description}</p>
        {props.children}
    </Card>
);

export default ModeCard;