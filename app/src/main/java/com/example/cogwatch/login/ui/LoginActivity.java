package com.example.cogwatch.login.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cogwatch.R;
import com.example.cogwatch.login.ui.view.TypewriterTextView;
import com.example.bridge.main.ChildrenActivity;

import main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private TypewriterTextView title;
    private TextView elder;
    private TextView children;
    private EditText username;
    private EditText password;
    private Button login;

    private Boolean isElder = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindView();

        bindClickListener();

        title.animateText("怎么称呼您?");
    }

    private void bindView() {
        title = findViewById(R.id.title);
        elder = findViewById(R.id.elder);
        children = findViewById(R.id.children);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
    }

    private void bindClickListener() {
        elder.setOnClickListener(v -> {
            isElder = true;
        });

        children.setOnClickListener(v -> {
            isElder = false;
        });

        login.setOnClickListener(v -> {
            Intent intent;
            if (isElder) {
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                intent = new Intent(this, ChildrenActivity.class);
                startActivity(intent);
            }
            finish();
        });
    }
}