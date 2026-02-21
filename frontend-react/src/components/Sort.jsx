import React from "react";

const Sort = ({theme, sortBy, setSortBy}) => {

    const handleChange = (event) => {
        const newSortBy = event.target.value;
        setSortBy(newSortBy);
        localStorage.setItem("sortBy", newSortBy);
    }

    return(
        <fieldset className={`absolute right-0 border rounded-lg px-1 text-wrap`}>
            <legend>Sort by</legend>
            <select onChange={handleChange} value={sortBy} className={`focus:outline-none ${theme==='light'?"bg-white":"bg-gray-900"}`}>
                <option value="publishedDate,desc">date: newest</option>
                <option value="publishedDate,asc">date: oldest</option>
                <option value="salary,desc">salary: highest</option>
                <option value="salary,asc">salary: lowest</option>
                <option value="company,asc">company: A-Z</option>
                <option value="company,desc">company: Z-A</option>
            </select>
        </fieldset>
    )
}

export default Sort;