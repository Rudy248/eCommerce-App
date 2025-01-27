package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerce.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;

public class ResetPasswordActivity extends AppCompatActivity {

    private String check = "";
    private TextView pageTitle, titleQues;
    private EditText phone, ques1, ques2;
    private Button verify_btn;
    private FloatingActionButton chatAdmin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        check=getIntent().getStringExtra("check");
        pageTitle=findViewById(R.id.page_title);
        titleQues=findViewById(R.id.title_ques);
        phone=findViewById(R.id.find_ph_no);
        ques1=findViewById(R.id.ques_1);
        ques2=findViewById(R.id.ques_2);
        verify_btn=findViewById(R.id.verify_btn);
        chatAdmin = findViewById(R.id.admin_fab);

        chatAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUrl("https://wa.me/+918016585820");
            }
        });
    }

    private void gotoUrl(String s) {
        Uri uri = Uri.parse(s);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    @Override
    protected void onStart() {
        super.onStart();
        phone.setVisibility(View.GONE);


        if(check.equals("settings")){
            pageTitle.setText("Security Questions");

            titleQues.setText("Set answers for the security questions: ");
            verify_btn.setText("Set");

            displayPrevAns();

            verify_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setAnswers();

                }
            });

        }
        else if(check.equals("login")){
            phone.setVisibility(View.VISIBLE);

            verify_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verifyUser();
                }
            });

        }
    }

    private void verifyUser() {
        final String phoneNo = phone.getText().toString();
        final String answer1= ques1.getText().toString().toLowerCase();
        final String answer2=ques2.getText().toString().toLowerCase();

        if(!phoneNo.equals("") && !answer1.equals("") && !answer2.equals("")){

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(phoneNo);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String mPhone = snapshot.child("phone").getValue().toString();

                        if(snapshot.hasChild("Security Questions")){
                            String ans1= snapshot.child("Security Questions").child("answer1").getValue().toString();
                            String ans2= snapshot.child("Security Questions").child("answer2").getValue().toString();

                            if(!ans1.equals(answer1)){
                                Toast.makeText(ResetPasswordActivity.this, "1st answer is wrong", Toast.LENGTH_SHORT).show();
                            }
                            else if(!ans2.equals(answer2)){
                                Toast.makeText(ResetPasswordActivity.this, "2nd answer is wrong", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);
                                builder.setTitle("New Password");

                                final EditText newPassword = new EditText(ResetPasswordActivity.this);
                                newPassword.setHint("Enter ");
                                builder.setView(newPassword);

                                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if(!newPassword.getText().toString().equals("")){
                                            reference.child("password").setValue(newPassword.getText()
                                                    .toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(ResetPasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();

                                    }
                                });
                                builder.show();
                            }
                        }
                        else{
                            Toast.makeText(ResetPasswordActivity.this, "You have not set the security questions yet. Please contact the admin.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(ResetPasswordActivity.this, "This user does not exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        else{
            Toast.makeText(this, "Please fill the details.", Toast.LENGTH_SHORT).show();
        }



    }

    private void setAnswers(){
        String ans1= ques1.getText().toString().toLowerCase();
        String ans2=ques2.getText().toString().toLowerCase();

        if(ques1.equals("")&&ques2.equals("")){
            Toast.makeText(ResetPasswordActivity.this, "Please answer both questions", Toast.LENGTH_SHORT).show();

        }
        else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());

            HashMap<String, Object> userdataMap= new HashMap<>();
            userdataMap.put("answer1", ans1);
            userdataMap.put("answer2", ans2);

            reference.child("Security Questions").updateChildren(userdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ResetPasswordActivity.this, "Security questions set successfully.", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ResetPasswordActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }

                }
            });
        }
    }

    private void displayPrevAns(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());
        reference.child("Security Questions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String answer1 = snapshot.child("answer1").getValue().toString();
                    String answer2 = snapshot.child("answer2").getValue().toString();

                    ques1.setText(answer1);
                    ques2.setText(answer2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}