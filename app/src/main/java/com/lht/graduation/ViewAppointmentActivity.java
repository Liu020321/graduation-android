package com.lht.graduation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ViewAppointmentActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://124.71.228.196";

    private Spinner spinnerPage;
    private ListView listViewAppointments;
    private ArrayAdapter<String> pageAdapter;
    private ArrayAdapter<String> appointmentAdapter;
    private ArrayList<String> appointmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointment);

        spinnerPage = findViewById(R.id.spinnerPage);
        listViewAppointments = findViewById(R.id.listViewAppointments);

        pageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        pageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPage.setAdapter(pageAdapter);

        appointmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listViewAppointments.setAdapter(appointmentAdapter);

        appointmentList = new ArrayList<>();

        spinnerPage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadAppointments(position + 1); // 加载指定页数的预约信息
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 初始化页面，默认加载第一页的预约信息
        loadAppointments(1);
    }

    // 加载总页数和当前页数到下拉框
    private void loadPages(int totalPages) {
        pageAdapter.clear();
        for (int i = 1; i <= totalPages; i++) {
            pageAdapter.add("第 " + i + " 页");
        }
    }

    // 加载指定页数的预约信息
    private void loadAppointments(int page) {
        // 获取用户ID
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        int userId = preferences.getInt("userId", -1); // 默认值为-1，表示未找到用户ID

        // 如果未找到用户ID，则直接返回
        if (userId == -1) {
            Toast.makeText(this, "未找到用户ID，请重新登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构建请求 URL
        String url = BASE_URL + "/phone_view_appointments?page=" + page + "&per_page=7"; // 每页显示7条数据

        // 发送 HTTP 请求获取预约信息
        new Thread(() -> {
            try {
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                // 添加用户ID到请求中
                JSONObject jsonInput = new JSONObject();
                jsonInput.put("user_id", userId);

                // 将用户ID写入请求体
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInput.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 读取服务器响应的数据
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // 解析 JSON 数据并更新 UI
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.toString());
                            JSONArray appointments = jsonResponse.getJSONArray("appointments");
                            appointmentList.clear();
                            for (int i = 0; i < appointments.length(); i++) {
                                JSONObject appointment = appointments.getJSONObject(i);
                                String department = appointment.getString("department_name");
                                String doctor = appointment.getString("doctor_name");
                                String time = appointment.getString("appointment_time");
                                int status = appointment.getInt("status");
                                String statusText = (status == 0) ? "已检查" : "未检查";
                                appointmentList.add("部门：" + department + "\n医生：" + doctor + "\n预约时间：" + time + "\n检查状态：" + statusText);
                            }
                            appointmentAdapter.clear();
                            appointmentAdapter.addAll(appointmentList);

                            // 获取总页数并更新下拉框选项
                            int totalPage = jsonResponse.getInt("total_pages");
                            loadPages(totalPage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ViewAppointmentActivity.this, "HTTP 请求失败", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ViewAppointmentActivity.this, "发生错误：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
