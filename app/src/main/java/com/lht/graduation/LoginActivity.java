// LoginActivity.java

package com.lht.graduation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Calendar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private OkHttpClient client;

    // 服务器基础URL
    private static final String BASE_URL = "http://192.168.162.99:5000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        client = new OkHttpClient();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                // 创建 JSON 对象
                JSONObject jsonObject = new JSONObject();
                try {
                    // 向 JSON 对象中添加用户名和密码
                    jsonObject.put("username", username);
                    jsonObject.put("password", password);

                    // 调用登录方法，传递 JSON 对象
                    loginUser(jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 登录用户
    private void loginUser(JSONObject jsonObject) {
        // 构建请求体
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

        // 构建请求
        Request request = new Request.Builder()
                .url(BASE_URL + "/phone_login")
                .post(body)
                .build();

        // 发起异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleLoginResponse(responseData);
                    }
                });
            }
        });
    }

    // 处理登录响应
    private void handleLoginResponse(String responseData) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            if (jsonResponse.has("username") && jsonResponse.has("user_id")) {
                String username = jsonResponse.getString("username");
                int userId = jsonResponse.getInt("user_id");

                // 输出日志以便调试
                Log.d("LoginActivity", "Login successful. Username: " + username + ", User ID: " + userId);

                // 保存登录时间
                saveLoginTime();
                // 保存登录状态和用户ID到 SharedPreferences
                saveLoginStatus(true, userId);

                // 进入主界面
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 关闭登录界面
            } else if (jsonResponse.has("error")) {
                String errorMessage = jsonResponse.getString("error");
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
        }
    }




    // 保存登录时间
    private void saveLoginTime() {
        long currentTimeMillis = System.currentTimeMillis(); // 获取当前时间的毫秒表示
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("loginTime", currentTimeMillis);
        Log.d("时间", "loginTime: " + currentTimeMillis);

        editor.apply(); // 异步保存
    }

    // 保存登录状态和用户ID到 SharedPreferences
    private void saveLoginStatus(boolean isLoggedIn, int userId) {
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.putInt("userId", userId);
        editor.apply();
    }

}
