import React from "react";
import JobCardField from "./JobCardField.jsx";

const JobCard = ({job, theme}) => {

    const hasAdditionalInfo = job.source || job.seniority || job.salary || job.employmentType;

    const themeClasses = theme === 'light'
        ? 'bg-white border-gray-300 hover:bg-gray-100 text-gray-900'
        : 'bg-gray-900 border-gray-500 hover:bg-gray-700 text-gray-300 hover:text-gray-100';

    return (
        <a href={job.url} target="_blank" rel="noopener noreferrer"
           className={`${themeClasses} block rounded-lg overflow-hidden shadow-md border hover:scale-105 transition w-full p-4`}>
            <h1 className="text-xl font-bold mb-2">{job.title}</h1>

            <div className="flex flex-col gap-1 text-sm">

                <JobCardField icon="🏢" label={job.company} tooltip="Company Name"/>
                <JobCardField icon="📍" label={job.location} tooltip="Job Location"/>
                <JobCardField icon="📅" label={job.publishedDate.substring(0,10)} tooltip="Published Date"/>

            </div>

            {hasAdditionalInfo && (
            <div className="flex flex-col gap-1 mt-3 pt-3 border-t">
                <span className="font-bold">Additional Info:</span>

                {job.seniority && (
                    <JobCardField icon="🏅" label={job.seniority} tooltip="Minimum Required Seniority"/>
                )}

                {job.employmentType && (
                    <JobCardField icon="💼" label={job.employmentType} tooltip="Employment Type"/>
                )}

                {job.salary && (
                    <JobCardField icon="💵" label={job.salary} tooltip="Salary"/>
                )}

                {job.source && (
                    <JobCardField icon="🔎" label={job.source} tooltip="Source Website"/>
                )}

            </div>
            )}

        </a>
    )
}
export default JobCard;