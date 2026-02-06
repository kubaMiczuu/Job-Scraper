import React, {useState, useEffect} from "react";
import JobList from "../components/JobList.jsx";
import SearchBar from "../components/SearchBar.jsx";
import mockJobs from "../mocks/jobs.json";

const Dashboard = () => {

    const [jobs, setJobs] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [loading, setLoading] = useState(true);
    const [theme, setTheme] = useState("light");

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setLoading(false);
        setJobs(mockJobs);
    }, [])

    const filteredJobs = jobs.filter(job =>
        job.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        job.company.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const toggleTheme = () => {
        setTheme(theme === "light" ? "dark" : "light");
    }

    const themeClasses = theme === 'light'
        ? 'bg-gray-50 text-gray-900'
        : 'bg-gray-900 text-gray-300';

    if(loading){
        return <div>Loading...</div>;
    }

    if(jobs.length === 0){
        return <div>No data available...</div>;
    }

    if(jobs.length > 0) {
        return (
            <div className={`${themeClasses} min-h-screen p-8 transition-all`}>

                <div className="relative flex flex-col justify-between items-center w-full mb-8">

                    <div className="absolute right-0 top-0 flex items-center gap-3">

                        <span className={`text-sm font-medium transition ${themeClasses}`}>Dark Theme</span>

                        <label className="relative inline-flex items-center cursor-pointer">
                            <input type="checkbox" className="sr-only peer" onChange={toggleTheme} checked={theme === 'dark'}/>
                            <div className="w-11 h-6 bg-gray-400 rounded-full peer peer-checked:bg-blue-600 peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-0.5 after:left-0.5 after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all"></div>
                        </label>

                    </div>

                    <span className={`transition block text-center text-4xl font-bold mb-5`}>Job Offers</span>

                    <div className={"w-full max-w-2xl"}>
                        <SearchBar searchTerm={searchTerm} setSearchTerm={setSearchTerm} theme={theme} />
                    </div>
                    <br/>

                    <JobList jobs={filteredJobs} theme={theme} />

                    <br/>
                    <p className={`${themeClasses} transition text-sm`}>
                        Showing {filteredJobs.length} of {jobs.length} jobs
                    </p>

                </div>
            </div>
        )
    }
}

export default Dashboard;