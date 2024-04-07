package org.example;

import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {

        Database database = Database.getInstance();

        database.insert("users",new Object[]{156,"ghasdjfa"});
        System.out.println(database.getDatabaseInfo());
        database.close();
    }
}