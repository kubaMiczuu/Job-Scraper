import React, {useCallback} from "react";
import SearchBar from "./SearchBar.jsx";
import JobList from "./JobList.jsx";
import Pagination from "./Pagination.jsx";
import RefreshButton from "./RefreshButton.jsx";

const Content = ({loading, error, jobLength, searchTerm, setSearchTerm, jobs, theme, currentPage, totalPages, setCurrentPage, fetchJobs, itemsPerPage, setItemsPerPage, lastRefresh}) => {
    if(loading) {
        return (
            <div className={`flex flex-row text-6xl border-t w-full justify-center items-center pt-50`}>
                <div>Loading</div>
                <span className="inline-block animate-pulse">.</span>
                <span className="inline-block animate-pulse animation-delay-200">.</span>
                <span className="inline-block animate-pulse animation-delay-400">.</span>
            </div>
        )
    }

    if(jobLength === 0) {
        return (
            <div className={`w-full flex flex-col justify-center items-center gap-10`}>
                <div className={`text-6xl pt-50 border-t w-full text-center`}>No data available</div>
                <button onClick={() => fetchJobs()} className={`w-1/4 text-4xl shadow-md duration-300 text-white py-3 px-2 rounded-lg bg-blue-600 hover:bg-blue-700 hover:scale-105 active:bg-blue-700 active:scale-95 active:duration-75 cursor-pointer`}>Retry</button>
            </div>
        )
    }

    if(error) {
        return (
            <div className={`w-full flex flex-col justify-center items-center gap-10`}>
                <div className={`text-6xl pt-50 border-t w-full text-center`}>Error: {error}</div>
                <button onClick={() => fetchJobs()} className={`w-1/4 text-4xl shadow-md duration-300 text-white py-3 px-2 rounded-lg bg-blue-600 hover:bg-blue-700 hover:scale-105 active:bg-blue-700 active:scale-95 active:duration-75 cursor-pointer`}>Retry</button>
            </div>
        )
    }

    return (
        <>
            <RefreshButton fetchJobs={fetchJobs} theme={theme} lastRefresh={lastRefresh}></RefreshButton>

            <SearchBar searchTerm={searchTerm} setSearchTerm={setSearchTerm} theme={theme} fetchJobs={fetchJobs} />

            <JobList jobs={jobs} theme={theme} />

            <Pagination currentPage={currentPage} totalPages={totalPages} theme={theme} setCurrentPage={setCurrentPage} itemsPerPage={itemsPerPage} setItemsPerPage={setItemsPerPage}></Pagination>
        </>
    )
}

export default Content;