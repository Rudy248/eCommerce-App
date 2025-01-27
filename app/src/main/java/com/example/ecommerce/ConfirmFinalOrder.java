package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ecommerce.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ConfirmFinalOrder extends AppCompatActivity {

    private EditText nameEditTxt, phoneEditTxt, addressEditTxt, cityEditTxt;
    private Button confirmOrderBtn;

    private String totalAmt="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_final_order);

        totalAmt=getIntent().getStringExtra("Total Price");
        Toast.makeText(this, "Total Price: " + totalAmt, Toast.LENGTH_SHORT).show();

        confirmOrderBtn= (Button) findViewById(R.id.confirm_final_order_btn);
        nameEditTxt=(EditText) findViewById(R.id.shipment_name);
        phoneEditTxt=(EditText) findViewById(R.id.shipment_phone_number);
        addressEditTxt=(EditText) findViewById(R.id.shipment_address);
        cityEditTxt=(EditText) findViewById(R.id.shipment_city);

        confirmOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Check();
            }
        });
    }

    private void Check() {
        if(TextUtils.isEmpty(nameEditTxt.getText().toString())){
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(phoneEditTxt.getText().toString())){
            Toast.makeText(this, "Phone Number is required", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(addressEditTxt.getText().toString())){
            Toast.makeText(this, "Address is required", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(cityEditTxt.getText().toString())){
            Toast.makeText(this, "City is required", Toast.LENGTH_SHORT).show();
        }
        else{
            ConfirmOrder();
        }

    }

    private void ConfirmOrder() {
        final String saveCurrentTime, saveCurrentDate;
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd:mm:yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm::ss a");
        saveCurrentTime = currentDate.format(callForDate.getTime());

        final DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders")
                .child(Prevalent.currentOnlineUser.getPhone());
        HashMap<String, Object> orderMap = new HashMap<>();
        orderMap.put("totalAmount", totalAmt);
        orderMap.put("name", nameEditTxt.getText().toString());
        orderMap.put("phone", phoneEditTxt.getText().toString());
        orderMap.put("address", addressEditTxt.getText().toString());
        orderMap.put("city", cityEditTxt.getText().toString());
        orderMap.put("date", saveCurrentDate);
        orderMap.put("time", saveCurrentTime);
        orderMap.put("state", "not shipped");

        ordersRef.updateChildren(orderMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference().child("Cart List")
                            .child("User View").child(Prevalent.currentOnlineUser.getPhone()).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(ConfirmFinalOrder.this, "Order placed successfully", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(ConfirmFinalOrder.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });

                }
            }
        });

    }
}