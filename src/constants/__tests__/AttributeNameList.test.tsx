import { getNameListWithHighestScoringAttributesRemoved } from "../AttributeNameList";
import { AttributeScoreObject } from "../AttributeScoreObject";

const ATTRIBUTES = ["STR", "COR", "STA", "PER", "INT", "PRS", "LUC"];

/**
 * Given a number representing the size of an array,
 * generate a random integer between 0 and arraySize - 1.
 * This number will be a valid index into the array.
 * 
 * @param arraySize Size of the array
 */
function getRandomArrayIndex(arraySize: number) {
    return Math.floor(Math.random() * arraySize);
}

/**
 * Generate a random number between -3 and 2.
 */
function generateNonHighestValueScore() {
    const values = [-3, -2, -1, 0, 1, 2];
    const randomArrayIndex = getRandomArrayIndex(6);

    return values[randomArrayIndex];
}

/**
 * Take the list of all attributes, select a certain number at random, and return
 * a list of the attributes that were selected as well as those that weren't.
 * 
 * @param numToInclude Number of attributes to include
 * @returns Object containing two lists, one of included attributed and other of excluded attributes
 */
function generateAttributeLists(numToInclude: number) {
    let includedAttributeList = [];
    let excludedAttributeList = [...ATTRIBUTES];

    // Remove n elements from the excluded list, and move them to the included list
    for (let i = 0; i < numToInclude; i++) {
        const arrayIndex = getRandomArrayIndex(excludedAttributeList.length);
        includedAttributeList.push(excludedAttributeList[arrayIndex]);
        excludedAttributeList.splice(arrayIndex, 1);
    }

    return { included: includedAttributeList, excluded: excludedAttributeList }
}

/**
 * Create an AttributeScoreObject with selected fields set to the highest possible value.
 * 
 * @param highestValueAttrList Fields to set to the highest possible value
 * @param nonHighestValueAttrList Fields to set to some other lower value
 * @returns AttributeScoreObject with fields set appropriately
 */
function generateAttributeScoreObj(highestValueAttrList: string[], nonHighestValueAttrList: string[]) {
    let attributeScoreObj: AttributeScoreObject = {
        STR: null,
        COR: null,
        STA: null,
        PER: null,
        INT: null,
        PRS: null,
        LUC: null
    };

    // Set the attributes that should be the highest to 3, the highest possible
    for (const highestAttr of highestValueAttrList) {
        const highestAttrKey = highestAttr as keyof AttributeScoreObject;
        attributeScoreObj[highestAttrKey] = 3;
    }

    // Set all other attributes to a value between -3 and 2
    for (const nonHighestAttr of nonHighestValueAttrList) {
        const nonHighestAttrKey = nonHighestAttr as keyof AttributeScoreObject;
        attributeScoreObj[nonHighestAttrKey] = generateNonHighestValueScore();
    }

    return attributeScoreObj;
}

test.each([
   "STR",
   "COR",
   "STA",
   "PER",
   "INT",
   "PRS",
   "LUC" 
])('setting %s to highest value removes it from the generated list', (attr: string) => {
    const expectedValueList = ATTRIBUTES.filter(a => a !== attr).sort();
    const attributeScoreObj = generateAttributeScoreObj([attr], expectedValueList);

    const nameList = getNameListWithHighestScoringAttributesRemoved(attributeScoreObj);
    const sortedValueNameList = nameList.map(x => x.value).sort();

    expect(sortedValueNameList.length).toBe(6);
    expect(sortedValueNameList).toEqual(expectedValueList);
});

test.each([
    2,
    3,
    4,
    5,
    6
])('setting %d random elements to highest value returns generated list with other elements', (numElements: number) => {
    const { included, excluded } = generateAttributeLists(numElements);
    const attributeScoreObj = generateAttributeScoreObj(included, excluded);

    const nameList = getNameListWithHighestScoringAttributesRemoved(attributeScoreObj);
    const sortedValueNameList = nameList.map(x => x.value).sort();

    expect(sortedValueNameList.length).toBe(7 - numElements);
    expect(sortedValueNameList).toEqual(excluded.sort());
});

test.each([
   -3,
   -2,
   -1,
   0,
   1,
   2,
   3 
])('setting all elements to %d returns list with all attributes', (scoreValue: number) => {
    const expectedValueList = [...ATTRIBUTES].sort();

    const attributeScoreObj: AttributeScoreObject = {
        STR: scoreValue,
        COR: scoreValue,
        STA: scoreValue,
        PER: scoreValue,
        INT: scoreValue,
        PRS: scoreValue,
        LUC: scoreValue
    }

    const nameList = getNameListWithHighestScoringAttributesRemoved(attributeScoreObj);
    const sortedValueNameList = nameList.map(x => x.value).sort();

    expect(sortedValueNameList.length).toBe(7);
    expect(sortedValueNameList).toEqual(expectedValueList);

});