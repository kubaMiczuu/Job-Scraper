import React from "react";

const SearchBar = ({searchTerm, setSearchTerm, theme}) => {

    const handleChange = (event) => {
        setSearchTerm(event.target.value);
    }

    const themeClasses = theme === 'light'
        ? 'bg-white border border-gray-300 placeholder-gray-900 hover:bg-gray-100 b'
        : 'bg-gray-900 placeholder-gray-300 hover:bg-gray-700 border border-gray-300';

    return (
        <div className="flex flex-row gap-4 w-full max-w-2xl">
            <input onChange={handleChange} value={searchTerm} placeholder="Search by title or company name..." className={`${themeClasses} focus:outline-none w-full p-4 rounded-lg overflow-hidden shadow-md hover:scale-105 transition h-8`}/>
        </div>
    )
}
export default SearchBar;