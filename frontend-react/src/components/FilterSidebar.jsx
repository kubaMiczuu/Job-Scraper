import React, {use, useEffect, useState} from 'react';
import FilterCategory from './FilterCategory.jsx';

const FilterSidebar = ({jobs, isOpen, onClose, filters, onFilterChange, theme, hasUnappliedFilters, handleApplyFilters}) => {

    const [seniorityOptions, setSeniorityOptions] = useState([]);
    const [employmentTypeOptions, setEmploymentTypeOptions] = useState([]);
    const [locationOptions, setLocationOptions] = useState([]);
    const [sourceOptions, setSourceOptions] = useState([]);

    const [seniorityExpand, setSeniorityExpand] = useState(false);
    const [employmentTypeExpand, setEmploymentTypeExpand] = useState(false);
    const [locationExpand, setLocationExpand] = useState(false);
    const [sourceExpand, setSourceExpand] = useState(false);

    useEffect(() => {
        const uniqueSeniorities = [...new Set(jobs.map(job => job.seniority))].filter(Boolean);
        setSeniorityOptions(uniqueSeniorities);

        const uniqueEmploymentTypes = [...new Set(jobs.map(job => job.employmentType))].filter(Boolean);
        setEmploymentTypeOptions(uniqueEmploymentTypes);

        const uniqueLocations = [...new Set(jobs.map(job => {
            return job.location.split(",")[0].trim();
        }))].filter(Boolean);
        setLocationOptions(uniqueLocations);

        const uniqueSources = [...new Set(jobs.map(job => job.source))].filter(Boolean);
        setSourceOptions(uniqueSources);

    }, [jobs])

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

    const handleExpand = (setExpand) => {
        setExpand(prev => !prev);
    }

    return (
        <>
            {isOpen && (
                <div className={`fixed inset-0 bg-black opacity-50 z-40 lg:hidden transition`} onClick={onClose}></div>
            )}

            <div className={`
                ${themeClasses}
                ${isOpen ? `translate-x-0` : '-translate-x-full'}
                fixed top-0 left-0 h-full w-80 p-5
                border-r shadow-xl z-50
                ease-in-out transform transition-transform duration-300
                lg:translate-x-0 lg:static  lg:z-0
                overflow-auto
                scrollbar-hide
            `}>

                <div className={`flex justify-between items-center mb-6`}>
                    <h2 className={`text-bold text-2xl`}>Filters</h2>
                    <button onClick={onClose} className={`${theme==="light" ? "hover:text-gray-900 hover:bg-gray-200" : "hover:text-gray-500 hover:bg-gray-900"} rounded-lg pb-2 px-2.5 lg:hidden text-2xl hover:scale-110 cursor-pointer active:scale-95 active:duration-75`}>x</button>
                </div>

                <button onClick={clearAllFilters} className={`bg-red-500 text-white rounded-lg hover:bg-red-600 transition hover:scale-105 duration-300 w-full mb-6 py-2 px-4 cursor-pointer active:duration-75 active:scale-95 ease-in-out active:bg-red-600`}>Clear All</button>

                <FilterCategory title={"Seniority"} options={seniorityOptions} selectedValue={filters.seniority} onToggle={(value) => {handleToggle('seniority', value)}} expand={seniorityExpand} onExpand={() => {handleExpand(setSeniorityExpand)}} theme={theme}/>

                <FilterCategory title={"Employment Type"} options={employmentTypeOptions} selectedValue={filters.employmentType} onToggle={(value) => {handleToggle('employmentType', value)}} expand={employmentTypeExpand} onExpand={() => {handleExpand(setEmploymentTypeExpand)}} theme={theme}/>

                <FilterCategory title={"Location"} options={locationOptions} selectedValue={filters.location} onToggle={(value) => {handleToggle('location', value)}} expand={locationExpand} onExpand={() => {handleExpand(setLocationExpand)}} theme={theme}/>

                <FilterCategory title={"Source"} options={sourceOptions} selectedValue={filters.source} onToggle={(value) => {handleToggle('source', value)}} expand={sourceExpand} onExpand={() => {handleExpand(setSourceExpand)}} theme={theme}/>

                <button onClick={()=>handleApplyFilters} className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 w-full hover:scale-105 active:bg-blue-700 active:duration-75 active:scale-95 ease-in-out duration-300">
                    Apply Filters
                    {hasUnappliedFilters && (
                    <span> ({Object.values(filters).flat().length} selected)</span>
                        )}
                </button>

            </div>
        </>
    )
}
export default FilterSidebar;