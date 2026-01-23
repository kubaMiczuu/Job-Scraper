package notifier;

import java.util.List;

public class App extends FakeJobProvider {

    private final List<Job> jobs;

    public App() {
        super();
        this.jobs = super.getJobs();
    }

    public static void main(String[] args) {
        App app = new App();
        for(int i=0;i<10;i++){
            Job currentJob = app.jobs.get(i);
            System.out.print(currentJob.id+", ");
            System.out.print(currentJob.title+", ");
            System.out.print(currentJob.company+", ");
            System.out.print(currentJob.location+", ");
            System.out.print(currentJob.date+"\n");
        }
    }
}
