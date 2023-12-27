import { DefaultOptionType } from "antd/es/select";
import { AttributeScoreObject } from "./AttributeScoreObject";

export const DEFAULT_ATTRIBUTE_NAMES: DefaultOptionType[] = [
    { value: 'STR', label: 'STR' },
    { value: 'COR', label: 'COR' },
    { value: 'STA', label: 'STA' },
    { value: 'PER', label: 'PER' },
    { value: 'INT', label: 'INT' },
    { value: 'PRS', label: 'PRS' },
    { value: 'LUC', label: 'LUC' },
];

/**
 * Given a object containing attribute scores, determine which scores are the highest,
 * and generate a list of attribute names where the highest scoring attributes have been removed.
 * 
 * @param attributeScoreObj 
 */
export function getNameListWithHighestScoringAttributesRemoved(attributeScoreObj: AttributeScoreObject) {
    const scoreList = Object.values(attributeScoreObj);

    // If all elements in the attribute score object have the same score, there's technically no
    // score that isn't the highest, so just return the list of all attribute names
    if (scoreList.every(x => x === scoreList[0])) {
        return DEFAULT_ATTRIBUTE_NAMES;
    }

    // No score can be lower than -3, so setting maxScore to this prior to looping through the scores
    // ensures we always find the maximum element
    let maxScore = -4;
    for (const score of scoreList) {
        if (score !== null) {
            const scoreNum = Number(score);
            if (scoreNum > maxScore) {
                maxScore = scoreNum;
            }
        }
    }

    // Generate the list where all elements with the max score have been removed
    let attributeNameList = DEFAULT_ATTRIBUTE_NAMES;
    for (const [attr, score] of Object.entries(attributeScoreObj)) {
        if (score === maxScore) {
            attributeNameList = attributeNameList.filter(x => x.value !== attr);
        }
    }

    return attributeNameList;
}