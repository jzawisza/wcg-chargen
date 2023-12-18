import { useState } from "react";
import { Select } from "antd";
import { DefaultOptionType } from "antd/es/select";

type SelectMultipleProps = {
    defaultValue: string[]
    disabled? : boolean
    numElementsAllowed: number
    onChange: (value: string[]) => void
    options: (DefaultOptionType[] | undefined)
    placeholder: string
};

// Wrapper around the Ant Design Select component to support selecting a finite number of elements.
// If numElementsAllowed is 1, this defaults to a standard Select invocation with no multiple selection.
const SelectMultiple = (props: SelectMultipleProps) => {
    const [selectedOptions, setSelectedOptions] = useState<string[]>([]);

    const selectMode = (props.numElementsAllowed > 1 ? 'multiple' : undefined);
    const isDisabled = (props.disabled ? props.disabled : false);

    const internalOnChange = (value: string[]) => {
        setSelectedOptions(value);
        props.onChange(value);
    }

    return (
        <Select
            allowClear
            defaultOpen={false}
            defaultValue={props.defaultValue}
            disabled={isDisabled}
            mode={selectMode}
            onChange={internalOnChange}
            open={selectedOptions === undefined || selectedOptions.length < props.numElementsAllowed}
            options={props.options}
            placeholder={props.placeholder}
            style={{ width: 150}}
        />
    );
}

export default SelectMultiple;