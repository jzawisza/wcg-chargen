import { Card, Popover } from "antd";
import { InfoCircleOutlined } from '@ant-design/icons';
import { ReactNode } from "react";

type SpeciesCardProps = {
    species: string,
    description: string,
    traits: ReactNode,
    selected: boolean
};

const SpeciesCard = (props: SpeciesCardProps) => {
    const classNameStr = "speciesCard" + (props.selected ? " speciesCard-selected" : "");

    return (
        <Card title={props.species}           
            extra={
                <Popover content={props.traits} title="Traits">
                    <InfoCircleOutlined />
                </Popover>
            }
            className={classNameStr}>
            <p>{props.description}</p>
        </Card>
    );
};

export default SpeciesCard;