/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zettix.terrain;

// database
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author sean
 */
public class DataBaseHandler {
    private final String databaseFile;
    private Connection conn;
    private String manifest;
    
    public DataBaseHandler(String databaseFile) {
        this.databaseFile = databaseFile;
    }
    
    public boolean Connect() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch  (ClassNotFoundException e) {
            System.out.println("Could load database class! " + e);
            return false;
        }
        try {
                  conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
        } catch (SQLException e) {
            System.out.println("Could not connect to database! " + databaseFile + " " + e);
            return false;
        }
        return true;
    }
    
    public String getManifest() {
        if (manifest == null) {
            Statement stmt = null;
        
            String result = null;
            try {
                StringBuffer selectCommand = new StringBuffer("SELECT data FROM ")
                    .append("manifest;");
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(selectCommand.toString());
                while (rs.next()) {
                    result = rs.getString("data");
                    break;
                }
            } catch (SQLException e) {
                System.out.println("Failed to get manifest! " + e);
            }
            manifest = result;
        }
        return manifest;
    }
    
    public int getX() {
        if (manifest != null) {
            return Integer.parseInt(manifest.substring(0, manifest.indexOf(" ")));
        }
        return 0;
    }
    
    public int getY() {
        if (manifest != null) {
            return Integer.parseInt(manifest.substring(manifest.indexOf(" ") + 1));
        }
        return 0;
    }
    
    public byte[] getBlob(String table, String key) {
        Statement stmt = null;
        byte[] result = null;
        try {
            StringBuffer selectCommand = new StringBuffer("SELECT data FROM ")
                .append(table)
                    .append(" WHERE id='")
                    .append(key)
                    .append("';");
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(selectCommand.toString());
            while (rs.next()) {
                result = rs.getBytes("data");
                break;
            }
        } catch (SQLException e) {
            System.out.println("Failed to get data blob with key: " + key + " " + e);
        }
        return result;
    }   
}