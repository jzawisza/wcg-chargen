export const ATTRIBUTE_ARRAY_SIZE = 7;

const ATTRIBUTE_ARRAYS: AttributeArrayType[] = [];

class AttributeArrayType {
    arrayType: string;
    array: number[];

    static CHALLENGING = new AttributeArrayType("challenging", [2, 1, 1, 0, 0, -1, -2]);
    static HEROIC = new AttributeArrayType("heroic", [2, 2, 1, 0, 0, 0, -1]);

    constructor(arrayType: string, array: number[]) {
        this.arrayType = arrayType;
        this.array = array;
        ATTRIBUTE_ARRAYS.push(this);
    }

    toString() {
        return this.arrayType;
    }

    getArray() {
        return this.array
    }
}

export function getArrayByName(name: string) {
    return ATTRIBUTE_ARRAYS.find(x => (x.toString() === name));
}

export default AttributeArrayType;