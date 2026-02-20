import React, {useState} from "react";

const SearchBar = ({setSearchTerm, theme}) => {

    const [newSearchTerm, setNewSearchTerm] = useState("");

    const handleChange = (event) => {
        setNewSearchTerm(event.target.value);
    }

    const handleSearch = () => {
        setSearchTerm(newSearchTerm);
    }

    const themeClasses = theme === 'light'
        ? 'bg-white border border-gray-300 placeholder-gray-900 hover:bg-gray-100 b'
        : 'bg-gray-900 placeholder-gray-300 hover:bg-gray-700 border border-gray-300';

    return (
        <>
            <fieldset className={`absolute right-0 border rounded-lg px-1 text-wrap`}>
                <legend>Sort by</legend>
                <select className={`focus:outline-none ${theme==='light'?"bg-white":"bg-gray-900"}`}>
                    <option>date: newest</option>
                    <option>date: oldest</option>
                    <option>salary: highest</option>
                    <option>salary: lowest</option>
                    <option>company: A-Z</option>
                    <option>company: Z-A</option>
                </select>
            </fieldset>
            <div className="flex lg:flex-row flex-col items-center lg:gap-6 gap-2 w-full max-w-2xl mx-auto">
                <input onChange={handleChange} value={newSearchTerm} placeholder="Search by title, company name or keywords..." className={`${themeClasses} mt-1 focus:outline-none w-full p-4 rounded-lg overflow-hidden shadow-md hover:scale-105 transition h-8`}/>
                 <div className={`flex flex-row items-center gap-5 w-2/3`}>
                    <button onClick={handleSearch} className={`text-white p-1 rounded-lg lg:w-1/3 w-1/2 duration-200 bg-blue-600 hover:bg-blue-700 transition hover:scale-110 shadow-md cursor-pointer active:scale-95 active:bg-blue-700 active:duration-95 ease-in-out`}>Search</button>
                    <button onClick={handleChange} value={""} className={`text-white p-1 rounded-lg lg:w-1/3 w-1/2 duration-200 bg-red-500 hover:bg-red-600 transition hover:scale-110 shadow-md cursor-pointer active:scale-95 active:bg-red-600 active:duration-95 ease-in-out`}>Clear</button>
                 </div>
                 </div>
            <br/>

        </>

    )
}
export default SearchBar;