package com.ub.columndb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


public class TestCracker {
    private static final Logger LOG = LoggerFactory.getLogger(TestCracker.class);

    public static void main(String[] args) {
        ColumnDB<Integer> db = new ColumnDB<>();
//        db.addColumn("A", Arrays.asList(13, 16, 4, 9, 2, 12, 7, 1, 19, 3, 14, 11, 8, 6));
//        db.addColumn("B", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14));
//        List<Tuple<Integer, Integer>> scan1 = db.scan("A", 10, 14, "B");

        db.addColumn("A", Arrays.asList(7, 4, 1, 2, 8, 3, 6));
        db.addColumn("B", Arrays.asList(1, 2, 3, 4, 5, 6, 7));
        db.addColumn("C", Arrays.asList(1, 2, 3, 4, 5, 6, 7));

        List<Tuple<Integer, Integer>> scan1 = db.scan("A", 0, 3, "B");
        List<Tuple<Integer, Integer>> scan2 = db.scan("A", 0, 5, "C");
        List<List<Tuple<Integer, Integer>>> scan3 = db.scan("A", 0, 4, "B", "C");
        LOG.info("");

    }
}