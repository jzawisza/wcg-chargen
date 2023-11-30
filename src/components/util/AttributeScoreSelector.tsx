const challengingAttributeArray = [2, 1, 1, 0, 0, -1, -2];
const heroicAttributeArray = [2, 2, 1, 0, 0, 0, -1];

type AttributeScoreSelectorProps = {
    method: string
};

const AttributeScoreSelector = (props: AttributeScoreSelectorProps) => {
    // TODO: remove hardcoded value, support other methods
    const attributeArray = (props.method === "a_heroic") ? heroicAttributeArray : challengingAttributeArray;

    // Use a HTML table to represent the attributes, the draggable score values,
    // and the places where those scores can be dropped
    return (
        <table className="attributeTable">
            <tr>
                <th className="attributeTableHeaderCell">Attributes</th>
                <th className="attributeTableHeaderCell">Scores</th>
                <th className="attributeTableHeaderCell" />
                <th className="attributeTableHeaderCell">Values</th>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Strength (STR)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {attributeArray[0]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Coordination (COR)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {attributeArray[1]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Stamina (STA)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {attributeArray[2]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Perception (PER)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {attributeArray[3]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Intellect (INT)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {attributeArray[4]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Presence (PRS)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {attributeArray[5]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Luck (LUC)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {attributeArray[6]}
                    </div>
                </td>
            </tr>
        </table>
    );
};

export default AttributeScoreSelector;