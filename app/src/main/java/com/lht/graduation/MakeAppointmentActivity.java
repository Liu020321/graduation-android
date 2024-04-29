package com.lht.graduation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MakeAppointmentActivity extends AppCompatActivity {

    private Spinner spinnerDepartment;
    private Spinner spinnerDoctor;
    private Spinner spinnerTime;
    private Button btnBookAppointment;
    private TextView textViewDateTime;
    private EditText editTextDescription;
    private RadioGroup radioGroupRevisit;

    private ArrayAdapter<String> departmentAdapter;
    private ArrayAdapter<String> doctorAdapter;
    private ArrayAdapter<String> timeAdapter;

    private int currentDepartmentId;

    private Map<String, Integer> doctorIdMap;
    private Map<String, String> doctorScheduleMap;
    // 服务器基础URL
    private static final String BASE_URL = "http://124.71.228.196";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_appointment);

        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        spinnerTime = findViewById(R.id.spinnerTime);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        textViewDateTime = findViewById(R.id.textViewDateTime);
        editTextDescription = findViewById(R.id.editTextDescription);
        radioGroupRevisit = findViewById(R.id.radioGroupRevisit);

        departmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(departmentAdapter);

        doctorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        doctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctor.setAdapter(doctorAdapter);

        timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(timeAdapter);

        doctorIdMap = new HashMap<>();
        doctorScheduleMap = new HashMap<>();


        loadDepartments();

        spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                currentDepartmentId = position + 1;
                loadDoctors(currentDepartmentId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        spinnerDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDoctor = spinnerDoctor.getSelectedItem().toString();
                updateAppointmentTime(selectedDoctor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        btnBookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDoctorName = spinnerDoctor.getSelectedItem().toString();
                int selectedDoctorId = doctorIdMap.get(selectedDoctorName);
                String selectedTime = spinnerTime.getSelectedItem().toString();
                String selectedDateTime = textViewDateTime.getText().toString();
                String description = editTextDescription.getText().toString();
                int revisitValue = radioGroupRevisit.getCheckedRadioButtonId() == R.id.radioButtonYes ? 1 : 0;

                SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
                int userId = preferences.getInt("userId", -1); // 默认值为-1，表示未找到用户ID


                JSONObject appointmentData = new JSONObject();
                try {
                    appointmentData.put("doctor_id", selectedDoctorId);
                    appointmentData.put("time", selectedTime);
                    appointmentData.put("datetime", selectedDateTime);
                    appointmentData.put("description", description);
                    appointmentData.put("revisit", revisitValue);
                    appointmentData.put("user_id", userId); // 将用户ID添加到预约数据中
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 发送 JSON 数据到服务器
                sendAppointmentDataToServer(appointmentData);
            }
        });


        textViewDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                MakeAppointmentActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        textViewDateTime.setText(year + "-" + (month + 1) + "-" + dayOfMonth + " " + hourOfDay + ":" + minute);
                                    }
                                },
                                hour,
                                minute,
                                true
                        );
                        timePickerDialog.show();
                    }
                },
                year,
                month,
                dayOfMonth
        );
        datePickerDialog.show();
    }

    private void loadDepartments() {
        new LoadDepartmentsTask().execute(BASE_URL + "/get_department");
    }

    private void loadDoctors(int departmentId) {
        String url = BASE_URL +"/get_doctors?department_id=" + departmentId;
        new LoadDoctorsTask().execute(url);
    }

    private void loadTimes(JSONArray doctors) {
        doctorAdapter.clear();
        timeAdapter.clear();
        doctorIdMap.clear();
        doctorScheduleMap.clear();

        try {
            for (int i = 0; i < doctors.length(); i++) {
                JSONObject doctor = doctors.getJSONObject(i);
                int id = doctor.getInt("id");
                String name = doctor.getString("name");
                String schedule = doctor.getString("schedule");
                doctorAdapter.add(name);
                doctorIdMap.put(name, id);
                doctorScheduleMap.put(name, schedule);
            }

            if (doctorAdapter.getCount() > 0) {
                String selectedDoctor = doctorAdapter.getItem(0);
                updateAppointmentTime(selectedDoctor);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateAppointmentTime(String selectedDoctor) {
        String schedule = doctorScheduleMap.get(selectedDoctor);
        timeAdapter.clear();
        timeAdapter.add(schedule);
    }

    private class LoadDepartmentsTask extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return new JSONArray(response.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            if (result != null) {
                loadDepartmentsSuccess(result);
            } else {
                Toast.makeText(MakeAppointmentActivity.this, "无法加载部门信息", Toast.LENGTH_SHORT).show();
            }
        }

        private void loadDepartmentsSuccess(JSONArray result) {
            departmentAdapter.clear();
            try {
                for (int i = 0; i < result.length(); i++) {
                    JSONObject department = result.getJSONObject(i);
                    String departmentName = department.getString("department_name");
                    departmentAdapter.add(departmentName);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class LoadDoctorsTask extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return new JSONArray(response.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            if (result != null) {
                loadTimes(result);
            } else {
                Toast.makeText(MakeAppointmentActivity.this, "无法加载医生信息", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 发送 JSON 数据到服务器的方法
// 发送 JSON 数据到服务器的方法
    private void sendAppointmentDataToServer(JSONObject data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建 OkHttpClient 实例
                    OkHttpClient client = new OkHttpClient();

                    // 创建请求体
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody requestBody = RequestBody.create(JSON, data.toString());

                    // 创建 POST 请求
                    Request request = new Request.Builder()
                            .url(BASE_URL +"/phone_appointment")
                            .post(requestBody)
                            .build();

                    // 发送请求并获取响应
                    Response response = client.newCall(request).execute();

                    // 获取响应结果
                    String responseData = response.body().string();

                    // 根据不同的响应结果显示不同的消息
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleAppointmentResponse(responseData, response.code());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    // 处理预约响应的方法
    // 处理预约响应的方法
    private void handleAppointmentResponse(String responseData, int statusCode) {
        if (statusCode == 200) {
            // 预约成功
            Toast.makeText(MakeAppointmentActivity.this, "预约成功", Toast.LENGTH_SHORT).show();
        } else if (statusCode == 400) {
            // 显示预约时间不对的消息
            Toast.makeText(MakeAppointmentActivity.this, "预约时间不对", Toast.LENGTH_SHORT).show();
        } else {
            // 其他情况，显示通用错误消息
            Toast.makeText(MakeAppointmentActivity.this, "遇到错误，请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }



}
