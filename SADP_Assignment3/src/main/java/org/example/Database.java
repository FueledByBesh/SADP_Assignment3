package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Database {

    private static Database instance;
    private static Lock lock = new ReentrantLock();
    private JsonNode dbproperties;
    private Connection connection;


    private Database(){
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            dbproperties = objectMapper.readTree(this.getClass().getResourceAsStream("/db_config.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Database getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new Database();
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    private Connection getConnection() {
        String url = "jdbc:postgresql://" + dbproperties.get("hostname").asText() + ":" + dbproperties.get("port").asText() + "/" + dbproperties.get("dbname").asText();
        if (connection == null) {
            synchronized (this) {
                if (connection == null) {
                    try {
                        connection = DriverManager.getConnection(url, dbproperties.get("username").asText(), dbproperties.get("password").asText());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return connection;
    }

    public void close(){
        if(connection!=null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String getDatabaseInfo() {
        return "Hostname: " + dbproperties.get("hostname").asText() +
                ", Port: " + dbproperties.get("port").asText() +
                ", Username: " + dbproperties.get("username").asText() +
                ", Password: " + dbproperties.get("password").asText() +
                ", Database Name: " + dbproperties.get("dbname").asText();
    }

    public void insert(String tableName,Object[] data){

        StringBuilder sqlStatement = new StringBuilder("INSERT INTO "+tableName+" VALUES (");
        for (int i = 0; i < data.length; i++) {
            System.out.println(data[i]);
            if(i!=0)
                sqlStatement.append(", ");
            if(data[i] instanceof Integer || data[i] instanceof Boolean) {
                sqlStatement.append(data[i]);
                continue;
            }

            sqlStatement.append("'"+data[i]+"'");

        }
        sqlStatement.append(");");

        try (Statement statement = getConnection().createStatement()){
            System.out.println(statement.executeUpdate(sqlStatement.toString()));
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

}
