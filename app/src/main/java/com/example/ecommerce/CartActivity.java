package com.example.ecommerce;

import static java.lang.Integer.valueOf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerce.Model.Cart;
import com.example.ecommerce.Prevalent.Prevalent;
import com.example.ecommerce.ViewHolder.CarttViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button NextProcessBtn;
    private TextView txtTotalAmt, txtMsg1;

    private int overallTotalPrice=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        recyclerView= findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        NextProcessBtn = (Button) findViewById(R.id.next_btn_cart);
        txtTotalAmt=(TextView) findViewById(R.id.total_price_cart);
        txtMsg1 = (TextView)findViewById(R.id.msg1);


        NextProcessBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                txtTotalAmt.setText("Total Price: ₹" + String.valueOf(overallTotalPrice));
                Intent intent = new Intent(CartActivity.this, ConfirmFinalOrder.class);
                intent.putExtra("Total Price", String.valueOf(overallTotalPrice));
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkOrderState();

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        FirebaseRecyclerOptions<Cart> options= new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone()).child("Products"), Cart.class).build();

        FirebaseRecyclerAdapter<Cart, CarttViewHolder> adapter= new FirebaseRecyclerAdapter<Cart, CarttViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CarttViewHolder holder, int position, @NonNull Cart model) {
                holder.txtProductQuantity.setText("Quantity: "+model.getQuantity());
                holder.txtProductPrice.setText( "Price: "+model.getPrice());
                holder.txtProductName.setText(model.getPname());

                int oneProd = ((Integer.valueOf(model.getPrice())))*Integer.valueOf(model.getQuantity());
                overallTotalPrice=overallTotalPrice+oneProd;

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence options[]= new CharSequence[]{
                          "Edit",
                          "Delete"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options: ");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i == 0){
                                    Intent intent = new Intent(CartActivity.this, ProductDetailsActivity1.class);
                                    intent.putExtra("pid", model.getPid());
                                    startActivity(intent);
                                }
                                if(i== 1){
                                    cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone())
                                            .child("Products")
                                            .child(model.getPid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(CartActivity.this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(CartActivity.this, HomeActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        builder.show();
                    }
                });

            }

            @NonNull
            @Override
            public CarttViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                CarttViewHolder holder = new CarttViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
    private void checkOrderState(){
        DatabaseReference ordersRef;
        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String shippingState= snapshot.child("state").getValue().toString();
                    String userName= snapshot.child("name").getValue().toString();

                    if(shippingState.equals("shipped")){
                        txtTotalAmt.setText("Order Shipped");
                        recyclerView.setVisibility(View.GONE);

                        txtMsg1.setVisibility(View.VISIBLE);
                        txtMsg1.setText("Order Shipped");
                        NextProcessBtn.setVisibility(View.GONE);

                    }
                    else if(shippingState.equals("not shipped")){
                        txtTotalAmt.setText("Order yet to dispatch");
                        recyclerView.setVisibility(View.GONE);

                        txtMsg1.setVisibility(View.VISIBLE);
                        NextProcessBtn.setVisibility(View.GONE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}