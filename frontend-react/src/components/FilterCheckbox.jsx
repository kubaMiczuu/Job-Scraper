import React from "react";

const FilterCheckbox = ({label, onChange, checked, theme}) => {

    const themeClasses = theme === 'light'
        ? 'bg-gray-50 text-gray-900'
        : 'bg-gray-900 text-gray-300';

    return (
        <label className={`${themeClasses} flex items-center gap-2 cursor-pointer hover:bg-opacity-50 p-2 rounded hover:scale-105 hover:border origin-top`}>

            <input type="checkbox" checked = {checked} onChange={onChange} className={"accent-blue-500 w-4 h-4 cursor-pointer"}/>

            <span className="ml-2 text-sm">{label}</span>

        </label>
    )
}
export default FilterCheckbox;