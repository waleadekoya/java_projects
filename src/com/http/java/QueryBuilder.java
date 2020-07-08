package com.http.java;

public class QueryBuilder {

    private static String keyWord;
    private static String startingSalary;
    private static String howManyDaysBack;
    private static Boolean contractOnly;

    public static String getKeyWord() {
        return keyWord;
    }

    public static String getStartingSalary() {
        return startingSalary;
    }

    public static String getHowManyDaysBack() {
        return howManyDaysBack;
    }

    public static Boolean getContractOnly() {
        return contractOnly;
    }

    public static boolean isContract() {
        return getContractOnly();
    }

    public static void setKeyWord(String keyWord) {
        if (QueryBuilder.keyWordLengthIsMoreThanOne(keyWord)) {
            QueryBuilder.keyWord = String.join("+", keyWord.toLowerCase().split(" "));
        } else QueryBuilder.keyWord = keyWord;
    }

    public static void setStartingSalary(int startingSalary) {
        QueryBuilder.startingSalary = String.valueOf(startingSalary);
    }

    public static void setHowManyDaysBack(int howManyDaysBack) {
        QueryBuilder.howManyDaysBack = String.valueOf(howManyDaysBack);
    }

    public static void setContractOnly(Boolean contractOnly) {
        QueryBuilder.contractOnly = contractOnly;
    }

    private static boolean keyWordLengthIsMoreThanOne(String keyWord) {
        return keyWord.split(" ").length > 1;
    }

}
