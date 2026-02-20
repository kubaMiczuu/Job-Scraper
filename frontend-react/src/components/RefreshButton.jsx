import React from "react";

const RefreshButton = ({fetchJobs, theme, lastRefresh}) => {
    return (
        <div className={`flex flex-row absolute lg:left-0 left-30 top-0 items-center`}>
           <button onClick={() => fetchJobs()} className={`text-2xl mb-8 text-white rounded-lg bg-blue-600 px-2 py-1 ease-in-out hover:bg-blue-700 hover:scale-115 duration-200 transition active:bg-blue-700 active:scale-95 active:duration-75 cursor-pointer`}>⭮</button>
            <div className={`ml-3 mb-8 ${theme==='light'?"text-gray-900":"text-white"} flex flex-col gap-0`}>
                <span>Last refresh:</span>
                <span>{lastRefresh}</span>
            </div>
        </div>
    )
}
export default RefreshButton;