package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerce.Model.Products;
import com.example.ecommerce.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProductDetailsActivity1 extends AppCompatActivity {
    private ImageView productImage;
    private EditText numberButton;
    private TextView productPrice, productDesc, productName;
    private String productId="", state="Normal";
    private Button addToCartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details_1);

        productId=getIntent().getStringExtra("pid");

        addToCartBtn = (Button) findViewById(R.id.product_details_add_to_cart_btn);
        numberButton=(EditText) findViewById(R.id.number_btn);
        productImage=(ImageView) findViewById(R.id.product_image_details);
        productName=(TextView) findViewById(R.id.product_name_details);
        productDesc=(TextView) findViewById(R.id.product_description_details);
        productPrice=(TextView) findViewById(R.id.product_price_details);

        getProductDetails(productId);

        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(state.equals("Order Placed") || state.equals("Order Shipped")){
                    Toast.makeText(ProductDetailsActivity1.this, "Enjoy shopping more after shipment of the latest order", Toast.LENGTH_LONG).show();
                }
                else{
                    addingToCartList();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkOrderState();
    }

    private void checkOrderState(){
        DatabaseReference ordersRef;
        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String shippingState= snapshot.child("state").getValue().toString();

                    if(shippingState.equals("shipped")){
                        state = "Order Shipped";

                    }
                    else if(shippingState.equals("not shipped")){
                        state="Order Placed";

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addingToCartList() {
        String saveCurrentTime, saveCurrentDate;

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd:mm:yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm::ss a");
        saveCurrentTime = currentDate.format(callForDate.getTime());

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

        final HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put("pid", productId);
        cartMap.put("pname", productName.getText().toString());
        cartMap.put("price", productPrice.getText().toString());
        cartMap.put("date", saveCurrentDate);
        cartMap.put("time", saveCurrentTime);
        cartMap.put("quantity", numberButton.getText().toString());
        cartMap.put("discount", "");

        cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone()).child("Products").child(productId).updateChildren(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    cartListRef.child("Admin View").child(Prevalent.currentOnlineUser.getPhone())
                            .child("Products").child(productId).updateChildren(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(ProductDetailsActivity1.this, "Item added to cart", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ProductDetailsActivity1.this, HomeActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            });

                }
            }
        });

    }

    private void getProductDetails(String productId) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("Products");
        productsRef.child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Products products = snapshot.getValue((Products.class));

                    productName.setText(products.getPname());
                    productPrice.setText(products.getPrice());
                    productDesc.setText(products.getDescription());
                    Picasso.get().load(products.getImage()).into(productImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        
    }


}