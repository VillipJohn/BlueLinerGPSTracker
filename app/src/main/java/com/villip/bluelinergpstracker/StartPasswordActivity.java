package com.villip.bluelinergpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class StartPasswordActivity extends AppCompatActivity {
    private EditText mAdminPasswordEditText;
    private Button mOkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_password);

        mAdminPasswordEditText = (EditText) findViewById(R.id.adminPasswordEditText);
        mOkButton = (Button) findViewById(R.id.okButton);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdminPasswordEditText.getText().toString().equals("qwerty123456")){
                    Intent intent = new Intent(StartPasswordActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdminPasswordEditText.setText("");
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();

    }
}
