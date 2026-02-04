import React from "react";
import JobCardField from "./JobCardField.jsx";

const JobCard = ({job}) => {

    const hasAdditionalInfo = job.source || job.seniority || job.salary || job.employmentType;

    return (
        <a href={job.url} target="_blank" rel="noopener noreferrer"
           className=" block bg-white-200 rounded-lg overflow-hidden shadow-md border border-gray-200 hover:bg-gray-100 hover:scale-105 transition w-full p-4">
            <h1 className="text-xl font-bold text-gray-800 mb-2">{job.title}</h1>

            <div className="flex flex-col gap-1 text-sm text-gray-600">

                <JobCardField icon="🏢" label={job.company} tooltip="Company Name"/>
                <JobCardField icon="📍" label={job.location} tooltip="Job Location"/>
                <JobCardField icon="📅" label={job.publishedDate} tooltip="Published Date"/>

            </div>

            {hasAdditionalInfo && (
            <div className="flex flex-col gap-1 mt-3 pt-3 border-t border-gray-200 text-sm text-gray-600">
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