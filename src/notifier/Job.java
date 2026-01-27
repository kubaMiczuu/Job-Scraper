package notifier;

public class Job {

    public String title;
    public String company;
    public String location;
    public String url;
    public String date;
    public String jobState;

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
