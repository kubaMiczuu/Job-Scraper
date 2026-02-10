import React from "react";
import FilterCheckbox from './FilterCheckbox.jsx';

const FilterCategory = ({title, options, selectedValue, onToggle, theme}) => {

    const themeClasses = theme === 'light'
        ? 'text-gray-900 border-gray-300'
        : 'text-gray-300 border-gray-600';

    return (
        <div className={`${themeClasses} flex flex-col mb-6 pb-4 border-b`}>

            <h3 className={"font-bold text-lg mb-3"}>{title}</h3>

            <div className="flex flex-col gap-2">

                {options.map((option) => (
                    <FilterCheckbox
                    key={option}
                    label={option[0]+option.substring(1,option.length).toLowerCase()}
                    onChange={() => onToggle(option)}
                    checked={selectedValue.includes(option)}
                    theme={theme}
                    />
                ))}

            </div>

        </div>
    )
}
export default FilterCategory;