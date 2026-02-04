import React, {useState, useEffect} from "react";
import JobCard from "../components/JobCard";
import mockJobs from "../mocks/jobs.json";

const Dashboard = () => {

    const [jobs, setJobs] = useState([]);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setJobs(mockJobs);
    }, [])

    return (
        <div className="m-3">
            <span className="block text-center text-4xl font-bold mb-5">Job Offers</span>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
                {jobs.map((job, index) => (
                    <JobCard key={index} job={job} />
                ))}
            </div>
        </div>
    )
}

export default Dashboard;