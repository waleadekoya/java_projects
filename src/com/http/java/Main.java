package com.http.java;

public class Main implements Runnable {

    public static void main(String[] args) {
        Thread main = new Thread(new Main());
        main.start();

    }

    public static synchronized void caller() throws InterruptedException {
        QueryBuilder.setContractOnly(true);
        QueryBuilder.setHowManyDaysBack(1);
        QueryBuilder.setKeyWord("python");
        QueryBuilder.setStartingSalary(85000);

        Thread reed = new Thread(new ReedJobs());
        reed.start();
        Thread indeed = new Thread(new IndeedJobs());
        indeed.start();
        Thread totalJobs = new Thread(new TotalJobs());
        totalJobs.start();
        Thread cwJobs = new Thread(new CWJobs());
        cwJobs.start();
        Thread cvLibrary = new Thread(new CVLibrary());
        cvLibrary.start();

        reed.join();
        indeed.join();
        totalJobs.join();
        cwJobs.join();
        cvLibrary.join();
    }

    @Override
    public void run() {
        final long startTime = System.currentTimeMillis();
        try {
            caller();
            DataModel.saveTable(QueryBuilder.getKeyWord());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime));
    }
}
