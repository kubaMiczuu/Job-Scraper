import React from "react";

const SearchBar = ({searchTerm, setSearchTerm, theme, fetchJobs}) => {

    const handleChange = (event) => {
        setSearchTerm(event.target.value);
    }

    const themeClasses = theme === 'light'
        ? 'bg-white border border-gray-300 placeholder-gray-900 hover:bg-gray-100 b'
        : 'bg-gray-900 placeholder-gray-300 hover:bg-gray-700 border border-gray-300';

    return (
        <>
            <div className="flex flex-row gap-6 w-full max-w-2xl">
                <button onClick={() => fetchJobs(1)} className={`text-2xl text-white rounded-lg bg-blue-600 px-2 py-1 hover:bg-blue-700 hover:scale-115 duration-200 transition active:bg-blue-700 active:scale-95 active:duration-75 cursor-pointer`}>⭮</button>
                <input onChange={handleChange} value={searchTerm} placeholder="Search by title, company name or keywords..." className={`${themeClasses} mt-1 focus:outline-none w-full p-4 rounded-lg overflow-hidden shadow-md hover:scale-105 transition h-8`}/>
                <button onClick={handleChange} value={""} className={`text-white p-1 rounded-lg w-1/6 duration-200 bg-red-500 hover:bg-red-600 transition hover:scale-110 shadow-md cursor-pointer active:scale-95 active:bg-red-600 active:duration-95 ease-in-out`}>Clear</button>
            </div>
            <br/>
        </>

    )
}
export default SearchBar;