// MainActivity.java
package com.lht.graduation;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/graduation";
    private static final String USER = "root";
    private static final String PASS = "12345678";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textView);
        String result = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM doctor");

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("department_id");
                String schedule = rs.getString("schedule");
                result += "ID: " + id + ", Name: " + name + ", Schedule: " + schedule + "\n";
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        textView.setText(result);
    }
}