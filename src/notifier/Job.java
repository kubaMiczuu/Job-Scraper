package notifier;

import java.util.Date;

public class Job {

    protected int id;
    protected String title;
    protected String company;
    protected String location;
    protected String url;
    protected Date date;

    public Job(int id, String title, String company, String location, String url, Date date) {
        setId(id);
        setTitle(title);
        setCompany(company);
        setLocation(location);
        setUrl(url);
        setDate(date);
    }

    public void setId(int id) {
        this.id = id;
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

    public void setDate(Date date) {
        this.date = date;
    }
}
