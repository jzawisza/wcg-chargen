import { useDroppable } from "@dnd-kit/core";
import { ScoreStyle } from "../../../constants/AttributeScoreStyle";

type AttributeDndScoreCellProps = {
    attributeShortName: string;
    score: number | null;
};

// Table cell that either contains an attribute score or is droppable so a score can be dragged here
const AttributeDndScoreCell = (props: AttributeDndScoreCellProps) => {
    const {isOver, setNodeRef} = useDroppable({
        id: `droppable${props.attributeShortName}`
    });
    const style = {
        backgroundColor: isOver ? 'silver' : undefined,
    };

    const customStyle = (props.score !== null) ? ScoreStyle : {};

    return (
        <td className="attributeTableScoreCell" ref={setNodeRef} style={{...style, ...customStyle}}>
            {props.score}
        </td>
    );
};

export default AttributeDndScoreCell;