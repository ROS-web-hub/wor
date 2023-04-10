package com.app.wor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.app.wor.models.UsersModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SignInActivity extends BaseActivity {

    DatabaseReference databaseReference;
    private static final String TAG = "EmailPasswordActivity";
    EditText edtEmail, editPassword;
    Button btnLogin;
    String email, passowrd;
    List<UsersModelClass> list;
    boolean flag = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        list = new ArrayList<>();

        edtEmail = findViewById(R.id.edtEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);

        loadUsersData();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = edtEmail.getText().toString().trim();
                passowrd = editPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    edtEmail.setError("Required!");
                    edtEmail.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(passowrd)){
                    editPassword.setError("Required!");
                    editPassword.requestFocus();
                    return;
                }

                Gson gson = new Gson();
                String json;
                SharedPreferences sharedPreferences = getSharedPreferences("LoginData",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                for(UsersModelClass model : list){

                    if(email.equals(model.getUsername()) && passowrd.equals(model.getPassword())){
                        flag = true;
                        json = gson.toJson(model);
                        editor.putString("json",json);
                        editor.apply();
                        break;
                    }
                }
                if(flag){
                    flag = false;
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(SignInActivity.this, "Incorrect details!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void loadUsersData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    UsersModelClass modelClass = snapshot1.getValue(UsersModelClass.class);
                    list.add(modelClass);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = getSharedPreferences("LoginState",MODE_PRIVATE);

        boolean login = sharedPref.getBoolean("login",false);
        if(login){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        super.onBackPressed();
    }
}