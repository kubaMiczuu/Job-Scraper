import React, {useState, useEffect} from "react";
import Content from "../components/Content.jsx";
import ThemeButton  from "../components/ThemeButton.jsx";
import FilterSidebar from "../components/FilterSidebar.jsx";

const Dashboard = () => {

    const [jobs, setJobs] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [theme, setTheme] = useState("dark");
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(null);

    const fetchJobs = async (page = 1, size= 2) => {
        try {
            setLoading(true);
            setError(null);

            const params = new URLSearchParams({
                page: page-1,
                size: size
            });

            const response = await fetch(`http://localhost:8080/api/jobs/all?${params}`);

            if (!response.ok) {
                throw new Error(`HTTP error, status code: ${response.status}`);
            }

            const data = await response.json();

            setJobs(data.content || []);
            setCurrentPage(data.page + 1);
            setTotalPages(data.totalPages);

        } catch (error) {
            console.error('Failed to fetch jobs:', error);
            setError(error.message);
        } finally {
            setLoading(false);
        }
    }

    const [filters, setFilters] = useState({
        seniority: [],
        employmentType: [],
        location: [],
        source: []
        }
    );

    useEffect(() => {
        fetchJobs(currentPage);
    }, [currentPage])

    const filteredJobs = jobs.filter(job => {
        const matchesSearch =
            job.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
            job.company.toLowerCase().includes(searchTerm.toLowerCase()) ||
            job.techKeywords.some(keyword => keyword.toLowerCase().includes(searchTerm.toLowerCase()));

        const matchesSeniority =
            filters.seniority.length === 0 ||
            (job.seniority && filters.seniority.includes(job.seniority));

        const matchesEmploymentType =
            filters.employmentType.length === 0 ||
            (job.employmentType && filters.employmentType.includes(job.employmentType));

        const matchesLocation =
            filters.location.length === 0 ||
            (job.location && filters.location.some(location =>
                job.location.toLowerCase().includes(location.toLowerCase())
            ));

        const matchesSource =
            filters.source.length === 0 ||
            (job.source && filters.source.includes(job.source));

        return matchesSearch && matchesSeniority && matchesEmploymentType && matchesLocation && matchesSource;
    });

    const handleFilterChange = (category, values) => {
        setFilters(prevState => ({ ...prevState, [category]: values }));
    }

    const toggleTheme = () => {
        setTheme(theme === "light" ? "dark" : "light");
    }

    const themeClasses = theme === 'light'
        ? 'bg-gray-50 text-gray-900'
        : 'bg-gray-900 text-gray-300';

    return (
        <div className={`${themeClasses} min-h-screen flex`}>

            <FilterSidebar jobs={jobs} isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} filters={filters} onFilterChange={handleFilterChange} theme={theme} />

            <div className={`${themeClasses} flex-1 p-5 transition-all`}>

                <div className="relative flex flex-col justify-between items-center w-full mb-8 p-8">

                    <button onClick={() => {setSidebarOpen(true)}} className={`lg:hidden cursor-pointer absolute left-0 top-0 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 hover:scale-105 transition active:scale-95 active:bg-blue-700 active:duration-75 ease-in-out`}>📊 Filters</button>

                    <ThemeButton toggleTheme={toggleTheme} themeClasses={themeClasses} theme={theme} />

                    <span className={`lg:mt-0 mt-10 transition block text-center text-4xl font-bold mb-5`}>Job Offers</span>

                    <Content loading={loading} error={error} jobLength={jobs.length} filteredJobs={filteredJobs} searchTerm={searchTerm} setSearchTerm={setSearchTerm} theme={theme} totalPages={totalPages} currentPage={currentPage} setCurrentPage={setCurrentPage}></Content>

                </div>

            </div>

        </div>
    )
}

export default Dashboard;