const API_BASE_URL = "http://localhost:8080/api";

export const jobsApi = {

    async fetchJobs(params = {}) {
        const {
            page = 1,
            size = 12,
            seniority = [],
            employmentType = [],
            location = [],
            source = [],
            searchTerm = '',
            sort = 'publishedDate,desc'
        } = params;

        const queryParams = new URLSearchParams();
        queryParams.append('page', page-1);
        queryParams.append('size', size);

        seniority.forEach(seniority => queryParams.append('seniority', seniority));
        employmentType.forEach(employmentType => queryParams.append('employmentType', employmentType));
        location.forEach(location => queryParams.append('location', location));
        source.forEach(source => queryParams.append('source', source));

        if(searchTerm) {
            queryParams.append('searchTerm', searchTerm);
        }


        queryParams.append('sort',sort);


        const response = await fetch(`${API_BASE_URL}/jobs/all?${queryParams}`);

        if (!response.ok) {
            throw new Error(`HTTP error, status code: ${response.status}`);
        }

        return response.json();
    }
}