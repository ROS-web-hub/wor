package com.app.wor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.WindowManager;

import com.app.wor.models.ProductModelClass;
import com.app.wor.models.StoreModelClass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    public static final int SPLASH_TIME_OUT = 1400;
    DatabaseReference databaseReference, databaseReference2;
    public static List<StoreModelClass> storesList;
    public static List<String> combineList;
    public static List<ProductModelClass> productsList;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        db = new DatabaseHelper(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Stores");
        databaseReference2 = FirebaseDatabase.getInstance().getReference("Products");
        storesList = new ArrayList<>();
        combineList = new ArrayList<>();
        productsList = new ArrayList<>();

        if(isConnectionAvailable(this)){
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    storesList.clear();
                    combineList.clear();
                    combineList.add("Select store");
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                        StoreModelClass modelClass = snapshot1.getValue(StoreModelClass.class);
                        String str = modelClass.getWestCode()+", "+modelClass.getCustomerId()+", "+modelClass.getStreet()+", "+modelClass.getCity()+", "+modelClass.getChain();
                        storesList.add(modelClass);
                        combineList.add(str);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}});

            databaseReference2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    productsList.clear();
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                        ProductModelClass modelClass = snapshot1.getValue(ProductModelClass.class);
                        productsList.add(modelClass);
                    }
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}});
        }else {
            Thread obj = new Thread() {
                public void run() {
                    try {
                        sleep(1500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            storesList.clear();
                            combineList.clear();
                            combineList.add("Select store");
                            productsList.clear();
                            storesList = db.getAllStores();
                            for(StoreModelClass modelClass : storesList){
                                String str = modelClass.getWestCode()+", "+modelClass.getCustomerId()+", "+modelClass.getStreet()+", "+modelClass.getCity()+", "+modelClass.getChain();
                                combineList.add(str);
                            }
                            productsList = db.getAllProducts();
                            startActivity(new Intent(getApplicationContext(), SignInActivity.class));

                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
            };obj.start();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }
}