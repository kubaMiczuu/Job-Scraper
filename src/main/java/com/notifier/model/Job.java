package com.notifier.model;

/**
 * Represents a job posting entity within the notification system.
 * This class holds detailed information regarding the position,
 * company, and current status of the job offer.
 */
public class Job {

    // Required fields
    /** The job title. */
    private String title;

    /** The hiring company name. */
    private String company;

    /** The geographical location of work. */
    private String location;

    /** The direct source URL of job. */
    private String url;

    /** The publication date of the job posting. */
    private String publishedDate;

    // Optional fields
    /** The platform or site where the job was found. */
    private String source;

    /** The required experience level (e.g., Junior, Mid, Senior).  */
    private String seniority;

    /** The type of contract (e.g., B2B, Permanent). */
    private String employmentType;

    /** The offered salary range or specific amount. */
    private String salary;

    /**
     * Constructs a new Job instance with essential information.
     * @param title         Position title.
     * @param company       Company name.
     * @param location      Job location.
     * @param url           Link to the offer.
     * @param publishedDate Date of publication.
     */
    public Job(String title, String company, String location, String url, String publishedDate) {
        setTitle(title);
        setCompany(company);
        setLocation(location);
        setUrl(url);
        setPublishedDate(publishedDate);
    }

    // Setters

    /**
     * Updates the job title.
     * @param title The new title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Updates the company name.
     * @param company The new company name to set.
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * Updates the job location.
     * @param location The new location to set.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Updates the application URL.
     * @param url The new URL to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Updates the published date.
     * @param publishedDate The published date to set.
     */
    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    /**
     * Updates the job origin source of the job.
     * @param source The origin source of the job to set.
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Updates the job required seniority.
     * @param seniority The new job seniority to set.
     */
    public void setSeniority(String seniority) {
        this.seniority = seniority;
    }

    /**
     * Updates the job employment type.
     * @param employmentType The new employment type to set.
     */
    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    /**
     * Updates the job salary range or exact value.
     * @param salary The new salary to set.
     */
    public void setSalary(String salary) {
        this.salary = salary;
    }

    // Getters

    /** @return The job title */
    public String getTitle() {
        return title;
    }

    /** @return The job company name */
    public String getCompany() {
        return company;
    }

    /** @return The job location */
    public String getLocation() {
        return location;
    }

    /** @return The application URL */
    public String getUrl() {
        return url;
    }

    /** @return The published date */
    public String getPublishedDate() {
        return publishedDate;
    }

    /** @return The origin source of job */
    public String getSource() {
        return source;
    }

    /** @return The job seniority */
    public String getSeniority() {
        return seniority;
    }

    /** @return The employment type of job */
    public String getEmploymentType() {
        return employmentType;
    }

    /** @return The job salary */
    public String getSalary() {
        return salary;
    }
}