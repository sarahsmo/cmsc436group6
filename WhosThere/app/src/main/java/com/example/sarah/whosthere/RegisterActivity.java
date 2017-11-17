package com.example.sarah.whosthere;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();
                View focusView = null;
                boolean cancel = false;

                if (TextUtils.isEmpty(password)) {
                    mPasswordView.setError(getString(R.string.error_field_required));
                    focusView = mPasswordView;
                    cancel = true;
                }

                // Check for a valid email address.
                if (TextUtils.isEmpty(email)) {
                    mEmailView.setError(getString(R.string.error_field_required));
                    focusView = mEmailView;
                    cancel = true;
                }

                if(!email.contains("@")){
                    mEmailView.setError(getString(R.string.error_invalid_email));
                    focusView = mEmailView;
                    cancel = true;
                }

                if(cancel){
                    focusView.requestFocus();
                }else{
                    storeIntentData();

                }
            }
        });

    }


    private void storeIntentData() {
        Intent data = new Intent();

        data.putExtra("email", mEmailView.getText().toString());
        data.putExtra("password", mPasswordView.getText().toString());

        setResult(RESULT_OK, data);
        // return data Intent and finish
        finish();
    }

}
