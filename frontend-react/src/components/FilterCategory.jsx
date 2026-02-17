import React from "react";
import FilterCheckbox from './FilterCheckbox.jsx';

const FilterCategory = ({title, options, selectedValue, onToggle, expand, onExpand, theme}) => {

    const themeClasses = theme === 'light'
        ? 'text-gray-900 border-gray-300'
        : 'text-gray-300 border-gray-600';

    return (
        <div className={`${themeClasses} flex flex-col mb-6 pb-4 border-b`}>

            <div onClick={onExpand} className={`flex flex-row justify-between cursor-pointer hover:scale-105`}>
                <h3 className={"font-bold text-lg mb-3"}>{title}</h3>
                <h3 className={`${theme === 'light' ? 'hover:bg-gray-200' : 'hover:bg-gray-900'} font-bold text-lg mb-3 mr-7 hover:scale-115 transition-transform duration-300 rounded-xl pt-1 pb-1 pr-2.5 pl-2.5 ${expand ? "rotate-z-90": ""} active:scale-95 active:duration-75 `}>▷</h3>
            </div>

            <div className={`grid transition-[grid-template-rows opacity] duration-300 ease-in-out ${expand ? "grid-rows-[1fr] opacity-100" : "grid-rows-[0fr] opacity-0"}`}>
                <div className="flex flex-col gap-2 overflow-hidden px-2 py-1">

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

        </div>
    )
}
export default FilterCategory;