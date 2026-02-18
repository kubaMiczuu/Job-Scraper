import React from "react";
import {getPaginationRange} from "../utils/getPaginationRange.js";
import PageField from "./PageField.jsx";


const PagesBar = ({setCurrentPage, currentPage, totalPages, theme}) => {

    const allPages = getPaginationRange(currentPage, totalPages);

    return (
        <>
            {allPages.map((page, index) => {
                if(page === "...") {
                    return <span key={index} className={`self-center px-1 font-bold`}>. . .</span>
                }
                return (
                    <PageField key={page} page={page} theme={theme} currentPage={currentPage} setCurrentPage={setCurrentPage}/>
                )
            })}
        </>
    )
}

export default PagesBar;
