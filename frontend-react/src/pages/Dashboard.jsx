import React, {useState, useEffect} from "react";
import JobList from "../components/JobList.jsx";
import SearchBar from "../components/SearchBar.jsx";
import ThemeButton  from "../components/ThemeButton.jsx";
import FilterSidebar from "../components/FilterSidebar.jsx";

const Dashboard = () => {

    const [jobs, setJobs] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [theme, setTheme] = useState("light");

    const fetchJobs = async () => {
        try {
            setLoading(true);
            setError(null);

            const response = await fetch('http://localhost:8080/api/jobs/all?limit=100');

            if (!response.ok) {
                throw new Error(`HTTP error, status code: ${response.status}`);
            }

            const jobs = await response.json();
            setJobs(jobs);
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
        fetchJobs();
    }, [])

    const filteredJobs = jobs.filter(job => {
        const matchesSearch =
            job.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
            job.company.toLowerCase().includes(searchTerm.toLowerCase());

        const matchesSeniority =
            filters.seniority.length === 0 ||
            (job.seniority && filters.seniority.includes(job.seniority));

        const matchesEmploymentType =
            filters.employmentType.length === 0 ||
            (job.employmentType && filters.employmentType.includes(job.employmentType));

        const matchesLocation =
            filters.location.length === 0 ||
            (job.location && filters.location.includes(job.location));

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

    if(loading){
        return <div>Loading...</div>;
    }

    if(error){
        return <div>Error: {error}</div>;
    }

    if(jobs.length === 0){
        return <div>No data available...</div>;
    }

    return (
        <div className={`${themeClasses} min-h-screen flex`}>

            <FilterSidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} filters={filters} onFilterChange={handleFilterChange} theme={theme} />

            <div className={`${themeClasses} flex-1 p-8 transition-all`}>

                <div className="relative flex flex-col justify-between items-center w-full mb-8">

                    <button onClick={() => {setSidebarOpen(true)}} className={`lg:hidden cursor-pointer absolute left-0 top-0 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition`}>📊 Filters</button>

                    <ThemeButton toggleTheme={toggleTheme} themeClasses={themeClasses} theme={theme} />

                    <span className={`transition block text-center text-4xl font-bold mb-5`}>Job Offers</span>

                    <SearchBar searchTerm={searchTerm} setSearchTerm={setSearchTerm} theme={theme} />

                    <JobList jobs={filteredJobs} theme={theme} />

                    <p className={`${themeClasses} transition text-sm`}>
                        Showing {filteredJobs.length} of {jobs.length} jobs
                    </p>

                </div>

            </div>

        </div>
    )
}

export default Dashboard;