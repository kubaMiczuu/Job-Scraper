import React, {useState, useEffect} from "react";

const SearchBar = ({setSearchTerm, theme}) => {

    const [newSearchTerm, setNewSearchTerm] = useState(localStorage.getItem("searchTerm") || "");
    const [innerValue, setInnerValue] = useState(localStorage.getItem("searchTerm"));

    useEffect(() => {
        const value = localStorage.getItem("searchTerm");
        const normalizedValue = value.replaceAll(",", " ");
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setInnerValue(normalizedValue);
    }, [])

    const handleChange = (event) => {
        const value = event.target.value;
        setNewSearchTerm(value);

        const normalizedValue = value.replaceAll(",", " ");
        setInnerValue(normalizedValue);
    }

    const handleSearch = () => {
        const normalizedSearchTerm = newSearchTerm.replaceAll(" ", ",");
        localStorage.setItem("searchTerm", normalizedSearchTerm);
        setSearchTerm(normalizedSearchTerm);
    }

    const themeClasses = theme === 'light'
        ? 'bg-white border border-gray-300 placeholder-gray-900 hover:bg-gray-100 b'
        : 'bg-gray-900 placeholder-gray-300 hover:bg-gray-700 border border-gray-300';


    return (
        <>
            <div className="flex lg:flex-row flex-col items-center lg:gap-6 gap-2 w-full max-w-2xl mx-auto">
                <input onChange={handleChange} value={innerValue} placeholder="Search by title, company name or keywords..." className={`${themeClasses} mt-1 focus:outline-none w-full p-4 rounded-lg overflow-hidden shadow-md hover:scale-105 transition h-8`}/>
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