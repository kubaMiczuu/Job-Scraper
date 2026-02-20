import React from "react";

const itemsPerPage = ({itemsPerPage, theme, fetchJobs, currentPage}) => {

    const handleChange = (event) => {
        const newSize = event.target.value;
        const firstItemIndex = (currentPage - 1)*itemsPerPage;
        const newPage = Math.floor(firstItemIndex/newSize)+1;
        fetchJobs(newPage, newSize);
    }

    return (
        <div className={`${theme==='light'?"text-gray-900":"text-gray-300"} text-md flex items-center gap-2 ml-4`}>
            <span>Items per page: </span>
            <select value={itemsPerPage} onChange={handleChange} className={`border rounded-lg p-1 focus:ring-1 focus:outline-none ${theme==='light'?"bg-white":"bg-gray-900"}`}>
                <option value="4">4</option>
                <option value="12">12</option>
                <option value="24">24</option>
                <option value="48">48</option>
                <option value="96" >96</option>
            </select>
        </div>
    )
}
export default itemsPerPage;