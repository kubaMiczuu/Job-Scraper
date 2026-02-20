import React, {useState, useEffect} from "react";
import Content from "../components/Content.jsx";
import ThemeButton  from "../components/ThemeButton.jsx";
import FilterSidebar from "../components/FilterSidebar.jsx";
import {useDebounce} from "../hooks/useDebounce.js";
import {jobsApi} from "../services/api.js";

const Dashboard = () => {

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [theme, setTheme] = useState(localStorage.getItem("theme") || "dark");

    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(null);
    const [itemsPerPage, setItemsPerPage] = useState(localStorage.getItem("itemsPerPage") || 12);
    const [lastRefresh, setLastRefresh] = useState(null);

    const [jobs, setJobs] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const debouncedSearchTerm = useDebounce(searchTerm, 300);

    const [pendingFilters, setPendingFilters] = useState({
            seniority: [],
            employmentType: [],
            location: [],
            source: []
        }
    );
    const [appliedFilters, setAppliedFilters] = useState({
            seniority: [],
            employmentType: [],
            location: [],
            source: []
        }
    );

    const fetchJobs = async () => {
        try {
            setLoading(true);
            setError(null);

            const response = jobsApi.fetchJobs({
                page: currentPage,
                size: itemsPerPage/**,
                ...appliedFilters,
                searchTerm: debouncedSearchTerm*/
            });

            const data = await response;

            setJobs(data.content || [])
            setCurrentPage(data.page + 1);
            setTotalPages(data.totalPages);

            const newSize = itemsPerPage;
            setItemsPerPage(newSize);
            localStorage.setItem('itemsPerPage', newSize);

            const hours = String(new Date().getHours()).padStart(2, '0');
            const minutes = String(new Date().getMinutes()).padStart(2, '0');
            const seconds = String(new Date().getSeconds()).padStart(2, '0');
            setLastRefresh(`${hours}:${minutes}:${seconds}`);

        } catch (error) {
            console.error('Failed to fetch jobs:', error);
            setError(error.message);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchJobs();
    }, [currentPage, itemsPerPage/**, appliedFilters, debouncedSearchTerm*/]);

    const toggleTheme = () => {
        const newTheme = theme === "dark" ? "light" : "dark";
        setTheme(newTheme);
        localStorage.setItem("theme", newTheme);
    }

    const themeClasses = theme === 'light'
        ? 'bg-gray-50 text-gray-900'
        : 'bg-gray-900 text-gray-300';

    return (
        <div className={`${themeClasses} min-h-screen flex overflow-hidden`}>

            <FilterSidebar jobs={jobs} isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} filters={pendingFilters} theme={theme} setCurrentPage={setCurrentPage} setPendingFilters={setPendingFilters} pendingFilters={pendingFilters} setAppliedFilters={setAppliedFilters} appliedFilters={appliedFilters} />

            <div className={`${themeClasses} flex-1 p-5 transition-all`}>

                <div className="relative flex flex-col justify-between items-center w-full mb-8 p-8">

                    <button onClick={() => {setSidebarOpen(true)}} className={`lg:hidden cursor-pointer absolute left-0 top-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 hover:scale-105 transition active:scale-95 active:bg-blue-700 active:duration-75 ease-in-out`}>📊 Filters</button>

                    <ThemeButton toggleTheme={toggleTheme} themeClasses={themeClasses} theme={theme} />

                    <span className={`lg:mt-0 mt-15 transition block text-center text-4xl font-bold mb-5`}>Job Offers</span>

                    <Content loading={loading} error={error} jobLength={jobs.length} jobs={jobs} searchTerm={searchTerm} setSearchTerm={setSearchTerm} theme={theme} totalPages={totalPages} currentPage={currentPage} setCurrentPage={setCurrentPage} fetchJobs={fetchJobs} itemsPerPage={itemsPerPage} setItemsPerPage={setItemsPerPage} lastRefresh={lastRefresh}></Content>

                </div>

            </div>

        </div>
    )
}

export default Dashboard;