import { getArrayByName } from "../AttributeArrayType";

test.each([
    ["challenging", [2, 1, 1, 0, 0, -1, -2]],
    ["heroic", [2, 2, 1, 0, 0, 0, -1]]
])('attribute array type %s has expected attribute values associated with it',
    (attributeArrayTypeStr: string, expectedAttributeArray: number[]) => {
        const attributeArrayType = getArrayByName(attributeArrayTypeStr);

        expect(attributeArrayType?.getArray()).toStrictEqual(expectedAttributeArray);
});