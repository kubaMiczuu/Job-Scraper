import React from "react";

const RefreshButton = ({fetchJobs}) => {
    return (
        <button onClick={() => fetchJobs(1)} className={`absolute left-0 top-0 text-2xl text-white rounded-lg bg-blue-600 px-2 py-1 hover:bg-blue-700 hover:scale-115 duration-200 transition active:bg-blue-700 active:scale-95 active:duration-75 cursor-pointer`}>⭮</button>
    )
}
export default RefreshButton;