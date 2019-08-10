package com.dujiajun.faceattendance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle("登录");
        setSupportActionBar(toolbar);

        EditText etName = findViewById(R.id.username);
        EditText etPass = findViewById(R.id.password);
        Button btnLogin = findViewById(R.id.login);
        AttendManager manager = AttendManager.getInstance(this);
        btnLogin.setOnClickListener(v -> manager.login(etName.getText().toString(), etPass.getText().toString(), new AttendManager.CallBack() {
            @Override
            public void onSuccess() {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailed() {
                Toast.makeText(LoginActivity.this, "登录失败，请检查用户名和密码", Toast.LENGTH_SHORT).show();
            }
        }));

        if (manager.checkCoursesExist()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}