import React from 'react';
import FilterCategory from './FilterCategory.jsx';

const FilterSidebar = ({isOpen, onClose, filters, onFilterChange, theme}) => {

    const themeClasses = theme === 'light'
        ? 'bg-white text-gray-900 border-gray-300'
        : 'bg-gray-800 text-gray-300 border-gray-700';

    const handleToggle = (category, value) => {
        const currentValues = filters[category];
        const newValues = currentValues.includes(value)
        ? currentValues.filter(v => v !== value) : [...currentValues, value];

        onFilterChange(category, newValues);
    }

    const clearAllFilters = () => {
        onFilterChange('seniority', [])
        onFilterChange('employmentType', [])
        onFilterChange('location', [])
        onFilterChange('source', [])
    }

    return (
        <>
            {isOpen && (
                <div className={`fixed inset-0 bg-black opacity-50 z-40 lg:hidden transition`} onClick={onClose}></div>
            )}

            <div className={`
                ${themeClasses}
                ${isOpen ? `translate-x-0` : '-translate-x-full'}
                fixed top-0 left-0 h-full w-80 p-4
                border-r shadow-xl z-50
                ease-in-out transform transition-transform duration-300
                lg:translate-x-0 lg:static  lg:z-0
                overflow-auto
                scrollbar-hide
            `}>

                <div className={`flex justify-between items-center mb-6`}>
                    <h2 className={`text-bold text-2xl`}>Filters</h2>
                    <button onClick={onClose} className={`lg:hidden text-2xl hover:text-gray-500 hover:scale-110`}>x</button>
                </div>

                <button onClick={clearAllFilters} className={`bg-red-500 text-white rounded-lg hover:bg-red-600 transition hover:scale-105 w-full mb-6 py-2 px-4 cursor-pointer`}>Clear All</button>

                <FilterCategory title={"Seniority"} options={['JUNIOR', 'MID', 'SENIOR', 'EXPERT']} selectedValue={filters.seniority} onToggle={(value) => {handleToggle('seniority', value)}} theme={theme}/>

                <FilterCategory title={"Employment Type"} options={['B2B', 'UOP', 'Internship']} selectedValue={filters.employmentType} onToggle={(value) => {handleToggle('employmentType', value)}} theme={theme}/>

                <FilterCategory title={"Location"} options={['Remote', 'Krakow, Poland', 'Warsaw, Poland']} selectedValue={filters.location} onToggle={(value) => {handleToggle('location', value)}} theme={theme}/>

                <FilterCategory title={"Source"} options={['pracuj.pl', 'LinkedIn', 'JustJoinIT']} selectedValue={filters.source} onToggle={(value) => {handleToggle('source', value)}} theme={theme}/>

            </div>
        </>
    )
}
export default FilterSidebar;