package com.http.java;

public interface UrlPagination {

    static int getPageCount(int results_count, int results_per_page) {

        if (results_count > 0) {
            if (results_count < results_per_page) {
                return 1;
            } else if (results_count % results_per_page > 0) {
                return Math.abs(results_count / results_per_page) + 1;
            }
        }
        return (results_count / results_per_page);
    }
}
