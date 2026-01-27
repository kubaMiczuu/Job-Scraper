package notifier;

public class Job {

    protected String title;
    protected String company;
    protected String location;
    protected String url;
    protected String date;
    protected String jobState;

    public Job(String title, String company, String location, String url, String date, String jobState) {
        setTitle(title);
        setCompany(company);
        setLocation(location);
        setUrl(url);
        setDate(date);
        setJobState(jobState);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setJobState(String jobState) {this.jobState = jobState;}
}
