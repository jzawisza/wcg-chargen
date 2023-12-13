import { useDraggable } from "@dnd-kit/core";

type AttributeDndValueCellProps = {
    index: number;
    score: number | null;
};

// Table cell containing an attribute score value.
// If the value is still present, i.e. it hasn't been dragged onto a score,
// the element will be draggable.
// Otherwise, the value is droppable, so scores that have alreadty been set
// can be dragged back here.
const AttributeDndValueCell = (props: AttributeDndValueCellProps) => {
    const {attributes, listeners, setNodeRef, transform} = useDraggable({
        id: `draggable${props.index}`,
    });
    const style = transform ? {
        transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
    } : undefined;

    return (
        <td className="attributeTableValueCell">
            {props.score !== null &&
                <div className="attributeScore" ref={setNodeRef} style={style} {...listeners} {...attributes}>
                    {props.score}
                </div>
            }
        </td>
    );
};

export default AttributeDndValueCell;