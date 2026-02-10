import React from 'react'
import JobCard from "../components/JobCard.jsx";

const JobList = ({jobs, theme}) => {
    return (
        <>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 w-full ">
                {jobs.map((job, index) => (
                    <JobCard key={index} job={job} theme={theme}/>
                ))}
            </div>
            <br/>
        </>
    )
}
export default JobList
