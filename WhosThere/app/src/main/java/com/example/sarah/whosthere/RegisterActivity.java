package com.example.sarah.whosthere;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Misha on 11/11/2017.
 */

public class RegisterActivity extends AppCompatActivity {

    //Just email and text for now
    private EditText mEmailView;
    private EditText mPasswordView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeIntentData();
            }
        });
    }


    private void storeIntentData() {
        Intent data = new Intent();

        //TODO - implement password logic

        data.putExtra("email", mEmailView.getText().toString());
        data.putExtra("password", mPasswordView.getText().toString());

        setResult(RESULT_OK, data);
        // return data Intent and finish
        finish();
    }

}
