package com.example.ecommerce.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ecommerce.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AdminMaintainProductsActivity extends AppCompatActivity {

    private Button applyChangesBtn, deleteProdBtn;
    private EditText name, price, desc;
    private ImageView imageView;
    private String productID="";
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_maintain_products);

        productID=getIntent().getStringExtra("pid");
        productsRef = FirebaseDatabase.getInstance().getReference().child("Products").child(productID);

        applyChangesBtn=findViewById(R.id.apply_changes_maintain_btn);
        name=findViewById(R.id.product_name_admin);
        price=findViewById(R.id.product_price_admin);
        desc=findViewById(R.id.product_description_admin);
        imageView=findViewById(R.id.product_image_admin);
        deleteProdBtn=findViewById(R.id.delete_prod_btn);

        displayProductInfo();

        applyChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyChanges();
            }
        });

        deleteProdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteItem();
            }
        });

    }

    private void deleteItem() {
        productsRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(AdminMaintainProductsActivity.this, AdminMaintainProductsActivity.class);
                startActivity(intent);
                finish();

                Toast.makeText(AdminMaintainProductsActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();


            }
        });
    }

    private void applyChanges() {
        String prname = name.getText().toString();
        String prprice = price.getText().toString();
        String prdesc = desc.getText().toString();

        if(prname.equals("")){
            Toast.makeText(this, "Write Product Name", Toast.LENGTH_SHORT).show();
        }
        else if(prprice.equals("")){
            Toast.makeText(this, "Write Product Price", Toast.LENGTH_SHORT).show();
        }
        else if(prdesc.equals("")){
            Toast.makeText(this, "Write Product Description", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String, Object> productMap=new HashMap<>();
            productMap.put("pid", productID);
            productMap.put("description", prdesc);
            productMap.put("price", prprice);
            productMap.put("pname", prname);

            productsRef.updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(AdminMaintainProductsActivity.this, "Changes applied successfully!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AdminMaintainProductsActivity.this, AdminCategoryActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
    }

    private void displayProductInfo() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String prname = snapshot.child("pname").getValue().toString();
                    String prprice = snapshot.child("price").getValue().toString();
                    String prdesc = snapshot.child("description").getValue().toString();
                    String primage = snapshot.child("image").getValue().toString();

                    name.setText(prname);
                    price.setText(prprice);
                    desc.setText(prdesc);
                    Picasso.get().load(primage).into(imageView);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}