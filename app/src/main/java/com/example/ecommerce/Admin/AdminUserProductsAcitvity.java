package com.example.ecommerce.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ecommerce.Model.Cart;
import com.example.ecommerce.R;
import com.example.ecommerce.ViewHolder.CarttViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminUserProductsAcitvity extends AppCompatActivity {

    private RecyclerView productsList;
    RecyclerView.LayoutManager layoutManager;
    private DatabaseReference cartListRef;
    private String userID="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_products_acitvity);

        userID = getIntent().getStringExtra("uid");
        productsList=findViewById(R.id.products_list_Admin);
        productsList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        productsList.setLayoutManager(layoutManager);

        cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List")
                .child("Admin View").child(userID).child("Products");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Cart> options = new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef, Cart.class).build();

        FirebaseRecyclerAdapter<Cart, CarttViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CarttViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CarttViewHolder holder, int position, @NonNull Cart model) {
                holder.txtProductQuantity.setText("Quantity: "+model.getQuantity());
                holder.txtProductPrice.setText( "Price: "+model.getPrice());
                holder.txtProductName.setText(model.getPname());

            }

            @NonNull
            @Override
            public CarttViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                CarttViewHolder holder = new CarttViewHolder(view);
                return holder;

            }
        };

        productsList.setAdapter(adapter);
        adapter.startListening();
    }
}