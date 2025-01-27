package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerce.Admin.AdminCategoryActivity;
import com.example.ecommerce.Model.Users;
import com.example.ecommerce.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {
    private EditText InputNumber, InputPassword;
    private Button LoginButton;
    private ProgressDialog loadingBar;
    private String ParentDbName = "Users";
    private CheckBox chkBoxRememberme;
    private TextView AdminLink, NotAdminLink, forgetPassLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginButton=(Button) findViewById(R.id.button);
        InputNumber=(EditText) findViewById(R.id.editTextNumber);
        InputPassword=(EditText) findViewById(R.id.editTextTextPassword);
        AdminLink=(TextView)findViewById(R.id.textView2);
        NotAdminLink=(TextView)findViewById(R.id.textView3);
        loadingBar=new ProgressDialog(this);
        forgetPassLink=findViewById(R.id.textView);

        chkBoxRememberme=(CheckBox)findViewById(R.id.checkBox);
        Paper.init(this);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginUser();
            }
        });


        forgetPassLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check", "login");
                startActivity(intent);
            }
        });

        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginButton.setText("Login Admin");
                AdminLink.setVisibility(View.INVISIBLE);
                NotAdminLink.setVisibility(View.VISIBLE);
                ParentDbName = "Admins";
            }
        });
        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginButton.setText("Login");
                AdminLink.setVisibility(View.VISIBLE);
                NotAdminLink.setVisibility(View.INVISIBLE);
                ParentDbName = "Users";
            }
        });
    }

    private void LoginUser() {
        String phone = InputNumber.getText().toString();
        String password = InputPassword.getText().toString();

        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Please write your phone number.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write your password.", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Logging In");
            loadingBar.setMessage("Please wait while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            AllowAccess(phone, password);
        }
    }

    private void AllowAccess(String phone, String password) {
        if(chkBoxRememberme.isChecked()){
            Paper.book().write(Prevalent.UserPhoneKey, phone);
            Paper.book().write(Prevalent.UserPasswordKey, password);
        }

        final DatabaseReference RootRef;
        RootRef= FirebaseDatabase.getInstance().getReference();
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(ParentDbName).child(phone).exists()) {
                    Users usersData=dataSnapshot.child(ParentDbName).child(phone).getValue(Users.class);
                    if(usersData.getPhone().equals(phone)){
                        if(usersData.getPassword().equals(password)){
                            if(ParentDbName.equals("Admins")){
                                Toast.makeText(LoginActivity.this, "Logged in successfully as an Admin.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent=new Intent(LoginActivity.this, AdminCategoryActivity.class);
                                startActivity(intent);
                            }
                            else if(ParentDbName.equals("Users")){
                                Toast.makeText(LoginActivity.this, "Logged in successfully.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent=new Intent(LoginActivity.this, HomeActivity.class);
                                Prevalent.currentOnlineUser=usersData;
                                startActivity(intent);

                            }
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User does not exist.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this, "Please create a new account.", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}