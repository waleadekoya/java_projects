package com.http.java;

import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DataModel {

    String salary, location, jobType, jobLink, jobTitle, advertiser;

    static ArrayList<String> titlesArray = new ArrayList<>();
    static ArrayList<String> salariesArray = new ArrayList<>();
    static ArrayList<String> locationsArray = new ArrayList<>();
    static ArrayList<String> contractTypesArray = new ArrayList<>();
    static ArrayList<String> urlsArray = new ArrayList<>();
    static ArrayList<String> advertisersArray = new ArrayList<>();


    public static Table createTable(String tableName, ArrayList<String> titlesArray, ArrayList<String> salariesArray,
                                    ArrayList<String> locationsArray, ArrayList<String> typesArray,
                                    ArrayList<String> urlsArray, ArrayList<String> advertisersArray) {
        StringColumn titleColumn = StringColumn.create("title", titlesArray);
        StringColumn salaryColumn = StringColumn.create("salary", salariesArray);
        StringColumn locationColumn = StringColumn.create("location", locationsArray);
        StringColumn typeColumn = StringColumn.create("type", typesArray);
        StringColumn urlColumn = StringColumn.create("url", urlsArray);
        StringColumn advertiserColumn = StringColumn.create("advertiser", advertisersArray);
        return Table.create(tableName, titleColumn, salaryColumn, locationColumn, typeColumn,
                advertiserColumn, urlColumn);
    }

    public static Table getTable() {
        Table t = DataModel.createTable(
                QueryBuilder.getKeyWord(), DataModel.titlesArray, DataModel.salariesArray,
                DataModel.locationsArray, DataModel.contractTypesArray, DataModel.urlsArray,
                DataModel.advertisersArray);
        System.out.println(t.print());
        System.out.println(t.shape());
        return t;
    }

    public static synchronized void saveTable(String tableName) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MMM_dd_hh_mma"));
        String fileName = String.format("%s_jobs_extract_%s.csv", tableName, now);
        try {
            DataModel.getTable().write().csv(fileName);
            System.out.println("File: " + fileName + " saved...");
        } catch (IOException e) {
            System.out.println(" There an error. \n: ");
            e.printStackTrace();
        }

    }

    public static synchronized void addToTable(String jobTitle, String salary, String location,
                                    String jobLink, String jobType, String advertiser) {
        DataModel.titlesArray.add(jobTitle);
        DataModel.salariesArray.add(salary);
        DataModel.locationsArray.add(location);
        DataModel.contractTypesArray.add(jobType);
        DataModel.urlsArray.add(jobLink);
        DataModel.advertisersArray.add(advertiser);
    }
}


