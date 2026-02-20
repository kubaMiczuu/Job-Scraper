import React from "react";

const RefreshButton = ({fetchJobs, theme, lastRefresh}) => {
    return (
        <div className={`flex flex-row text-md absolute left-0 top-0 items-center`}>
           <button onClick={() => fetchJobs(1)} className={`text-2xl text-white rounded-lg bg-blue-600 px-2 py-1 hover:bg-blue-700 hover:scale-115 duration-200 transition active:bg-blue-700 active:scale-95 active:duration-75 cursor-pointer`}>⭮</button>
            <span className={`ml-2 ${theme==='light'?"text-gray-900":"text-white"}`}>Last refresh: {lastRefresh} </span>
        </div>
    )
}
export default RefreshButton;