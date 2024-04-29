package com.lht.graduation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Button btnMakeAppointment;
    private Button btnViewAppointments;
    private Button btnLogout; // 添加退出登录按钮
    private ImageView imageViewLarge;
    private TextView textViewWelcome;
    private int[] imageArray = {R.mipmap.view1, R.mipmap.view2, R.mipmap.view3}; // 图片资源数组
    private int currentIndex = 0; // 当前显示图片的索引

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查用户登录状态
        if (!isUserLoggedIn() || isLoginExpired()) {
            Log.d("登录", "用户未登录或登录已过期");
            // 如果用户未登录或登录已过期，跳转到登录界面
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish(); // 结束当前活动，防止用户通过返回键回到主界面
            return;
        }

        btnMakeAppointment = findViewById(R.id.btnMakeAppointment);
        btnViewAppointments = findViewById(R.id.btnViewAppointments);
        btnLogout = findViewById(R.id.btnLogout); // 初始化退出按钮
        imageViewLarge = findViewById(R.id.imageViewLarge);
        textViewWelcome = findViewById(R.id.textViewWelcome);

        // 启动定时器，每隔一段时间切换图片
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 切换到下一张图片
                        currentIndex++;
                        if (currentIndex >= imageArray.length) {
                            currentIndex = 0; // 如果到达最后一张图片，重新从第一张开始
                        }
                        // 更新ImageView中的图片
                        imageViewLarge.setImageResource(imageArray[currentIndex]);
                    }
                });
            }
        }, 0, 3000); // 每隔3秒切换一次图片，可以根据需要调整间隔时间

        // 启用欢迎文字的循环滚动效果
        textViewWelcome.setSelected(true);
        textViewWelcome.setMarqueeRepeatLimit(-1);

        btnMakeAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到预约挂号界面
                Intent intent = new Intent(MainActivity.this, MakeAppointmentActivity.class);
                startActivity(intent);
            }
        });

        btnViewAppointments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到查看挂号界面
                Intent intent = new Intent(MainActivity.this, ViewAppointmentActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 执行注销操作
                logout();
            }
        });
    }

    // 检查用户是否已登录
    private boolean isUserLoggedIn() {
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        return preferences.getBoolean("isLoggedIn", false);
    }


    // 检查登录是否已过期
    private boolean isLoginExpired() {
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        long loginTime = preferences.getLong("loginTime", 0);
        long currentTimeMillis = System.currentTimeMillis();
        // 登录有效期为30天，单位为毫秒
        long expirationTime = 30L * 24 * 60 * 60 * 1000; // 30天
        Log.d("登录时间", "loginTime: " + loginTime);
        Log.d("当前时间", "currentTime: " + currentTimeMillis);
        return currentTimeMillis - loginTime > expirationTime;
    }

    // 注销方法
    private void logout() {
        // 清除登录状态和用户ID
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("isLoggedIn");
        editor.remove("userId");
        editor.apply();

        // 返回登录界面
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish(); // 结束当前活动，防止用户通过返回键回到主界面
    }
}
