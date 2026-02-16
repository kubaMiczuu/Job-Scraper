import React from "react";
import SearchBar from "./SearchBar.jsx";
import JobList from "./JobList.jsx";

const Content = ({loading, error, jobLength, searchTerm, setSearchTerm, jobs, filteredJobs, theme, themeClasses}) => {
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
        return <div className={`text-6xl pt-50 border-t w-full text-center`}>No data available</div>
    }

    if(error) {
        return <div className={`text-6xl pt-50 border-t w-full text-center`}>Error: {error}</div>
    }

    return (
        <>
            <SearchBar searchTerm={searchTerm} setSearchTerm={setSearchTerm} theme={theme} />

            <JobList jobs={filteredJobs} theme={theme} />

            <p className={`${themeClasses} transition text-sm`}>
                Showing {filteredJobs.length} of {jobs.length} jobs
            </p>
        </>
    )
}

export default Content;