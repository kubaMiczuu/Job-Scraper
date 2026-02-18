import React from "react";

const PaginationControl = ({setCurrentPage, page, text}) => {
    return (
        <button onClick={() => setCurrentPage(page)} className={`bg-blue-600 font-bold text-white hover:bg-blue-700 active:bg-blue-700 rounded-lg border py-2 px-4 cursor-pointer hover:scale-115 transition active:scale-95 active:duration-75 shadow-md`}>
            {text}
        </button>
    )
}
export default PaginationControl;