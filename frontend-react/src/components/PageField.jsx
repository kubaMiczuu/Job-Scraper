import React from "react";

const PageField = ({setCurrentPage, page, currentPage, theme}) => {

    const isActive = currentPage === page;

    const themeClasses = theme === 'light'
        ? 'bg-white border-gray-300 hover:bg-gray-200 text-gray-900'
        : 'bg-gray-900 border-gray-500 hover:bg-gray-700 text-gray-300 hover:text-gray-100';

    return (
        <button onClick={() => setCurrentPage(page)} className={` ${isActive ? "bg-blue-600 font-bold text-white hover:bg-blue-700 active:bg-blue-700" : themeClasses} rounded-lg border py-2 px-4 cursor-pointer hover:scale-115 transition active:scale-95 active:duration-75 shadow-md`}>
            {page}
        </button>
    )
}
export default PageField