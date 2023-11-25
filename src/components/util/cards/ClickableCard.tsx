import { Card } from "antd";
import { PropsWithChildren } from 'react';

type ClickableCardProps = {
    title: string,
    description: string
};

// Card that can be directly clicked on to transition to a new screen.
// Supports embedding components within the card.
const ClickableCard = (props: PropsWithChildren<ClickableCardProps>) => (
    <Card title={props.title}
        hoverable
        className="modeCard">
        <p>{props.description}</p>
        {props.children}
    </Card>
);

export default ClickableCard;