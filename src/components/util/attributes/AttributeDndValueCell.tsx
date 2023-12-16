import { useDraggable } from "@dnd-kit/core";
import { ScoreStyle } from "../../../constants/AttributeScoreStyle";

type AttributeDndValueCellProps = {
    index: number;
    score: number | null;
};

// Table cell containing a draggable attribute score value,
// or empty if the value has already been dragged to an attribute score element.
const AttributeDndValueCell = (props: AttributeDndValueCellProps) => {
    const {attributes, listeners, setNodeRef, transform} = useDraggable({
        id: `draggable${props.index}`,
    });
    const style = transform ? {
        transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
    } : undefined;

    return (
        <td className="attributeTableGeneralCell">
            {props.score !== null &&
                <div ref={setNodeRef} style={{...style, ...ScoreStyle}} {...listeners} {...attributes}>
                    {props.score}
                </div>
            }
        </td>
    );
};

export default AttributeDndValueCell;