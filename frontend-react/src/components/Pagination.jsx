import React from "react";
import PagesBar from "./PagesBar.jsx";
import PaginationControl from "./PaginationControl.jsx";
import ItemsPerPage from "./ItemsPerPage.jsx";
import itemsPerPage from "./ItemsPerPage.jsx";

const Pagination = ({setCurrentPage, currentPage, totalPages, theme, itemsPerPage, setItemsPerPage}) => {

    return (
        <div className="flex flex-row gap-3 justify-center">
            {currentPage > 1 && (
                <PaginationControl page={currentPage-1} setCurrentPage={setCurrentPage} text={"Previous"}></PaginationControl>
            )}

            <PagesBar totalPages={totalPages} theme={theme} setCurrentPage={setCurrentPage} currentPage={currentPage}></PagesBar>

            {currentPage < totalPages && (
                <PaginationControl page={currentPage+1} setCurrentPage={setCurrentPage} text={"Next"}></PaginationControl>
            )}

            <ItemsPerPage theme={theme} itemsPerPage={itemsPerPage} setItemsPerPage={setItemsPerPage} currentPage={currentPage} setCurrentPage={setCurrentPage}></ItemsPerPage>

        </div>
    )
}
export default Pagination;