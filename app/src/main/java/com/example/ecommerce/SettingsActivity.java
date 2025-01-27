package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerce.Prevalent.Prevalent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImageVIew;
    private EditText nameEditText, userPhoneEditText, addressEditText;
    private TextView profileChangeTxtBtn, closeTxtBtn, saveTxtBtn;
    private Uri imageUri;
    private String myUrl = "";
    private StorageReference storageProfilePictureRef;
    private String checker = "";
    private static final int GalleryPick=1;

    private StorageTask uploadTask;
    private Button securityQuesBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile pictures");
        profileImageVIew = (CircleImageView) findViewById(R.id.settings_profile_image);
        nameEditText = (EditText) findViewById(R.id.settings_name);
        userPhoneEditText = (EditText) findViewById(R.id.settings_phone_number);
        addressEditText = (EditText) findViewById(R.id.settings_address);
        profileChangeTxtBtn=(TextView) findViewById(R.id.profile_change_btn);
        closeTxtBtn=(TextView) findViewById(R.id.close_settings_btn);
        saveTxtBtn=(TextView) findViewById(R.id.update_settings_btn);
        securityQuesBtn=findViewById(R.id.security_questions_btn);


        userInfoDisplay(profileImageVIew, nameEditText, userPhoneEditText, addressEditText);

        closeTxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        securityQuesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check", "settings");
                startActivity(intent);
            }
        });

        saveTxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checker.equals("clicked")){
                    userInfoSaved();
                }
                else{
                    updateOnlyUserInfo();
                }
            }
        });

        profileImageVIew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        profileChangeTxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "clicked";
            }
        });
    }

    private void updateOnlyUserInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String, Object>userMap = new HashMap<>();
        userMap.put("name", nameEditText.getText().toString());
        userMap.put("address", addressEditText.getText().toString());
        userMap.put("phoneOrder", userPhoneEditText.getText().toString());
        ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
        finish();
    }

    private void OpenGallery(){
        Intent galleryIntent= new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null) {
            imageUri = data.getData();
            profileImageVIew.setImageURI(imageUri);
        }
        else{
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
            finish();
        }
    }

    private void userInfoSaved() {
        if(TextUtils.isEmpty(nameEditText.getText().toString())){
            Toast.makeText(this, "Name is mandatory.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(addressEditText.getText().toString())){
            Toast.makeText(this, "Address is mandatory.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(userPhoneEditText.getText().toString())){
            Toast.makeText(this, "Phone Number is mandatory.", Toast.LENGTH_SHORT).show();
        }
        else if(checker.equals("clicked")){
            uploadImage();
        }
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Update Profile");
        progressDialog.setMessage("Please wait, updating account info");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if(imageUri!= null){
            final StorageReference fileRef = storageProfilePictureRef.child(Prevalent.currentOnlineUser.getPhone()+".jpg");

            uploadTask=fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUrl = task.getResult();
                        myUrl=downloadUrl.toString();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

                        HashMap<String, Object>userMap = new HashMap<>();
                        userMap.put("name", nameEditText.getText().toString());
                        userMap.put("address", addressEditText.getText().toString());
                        userMap.put("phoneOrder", userPhoneEditText.getText().toString());
                        userMap.put("image", myUrl);
                        ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

                        progressDialog.dismiss();

                        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                        finish();

                    }
                    else{
                        Toast.makeText(SettingsActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }
        else{
            Toast.makeText(this, "Image is not selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void userInfoDisplay(CircleImageView profileImageVIew, EditText nameEditText, EditText userPhoneEditText, EditText addressEditText) {
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child("image").exists()){
                        String image = dataSnapshot.child("image").getValue().toString();
                        String name = dataSnapshot.child("name").getValue().toString();
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        String address = dataSnapshot.child("address").getValue().toString();

                        Picasso.get().load(image).into(profileImageVIew);
                        nameEditText.setText(name);
                        userPhoneEditText.setText(phone);
                        addressEditText.setText(address);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}