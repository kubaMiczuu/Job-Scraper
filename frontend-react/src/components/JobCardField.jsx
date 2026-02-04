import React from "react";

const JobCardField = ({icon, label, tooltip}) => {
    return (
        <div className="flex items-center gap-2 group relative w-fit cursor-default">
            <span>{icon}</span>
            <span>{label}</span>
            <div
                className="invisible group-hover:visible opacity-0 group-hover:opacity-100 transition-opacity absolute left-full ml-2 top-1/2 -translate-y-1/2 whitespace-nowrap bg-gray-800 text-white text-xs py-1 px-2 rounded shadow-lg">
                {tooltip}
                <div className="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-gray-800"></div>
            </div>
        </div>
    )
}
export default JobCardField;