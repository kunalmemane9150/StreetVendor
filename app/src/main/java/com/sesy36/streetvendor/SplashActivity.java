package com.sesy36.streetvendor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sesy36.streetvendor.fragments.LoginFragment;

public class SplashActivity extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();


        firebaseFirestore = FirebaseFirestore.getInstance();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = auth.getCurrentUser();
                if(user == null){
                    startActivity(new Intent(SplashActivity.this, FragmentReplacerActivity.class));
                    finish();
                }else {
                    checkUserType(user);
                }
            }
        }, 1000);
    }

    private void checkUserType(FirebaseUser user){
        //if user is seller, start seller main screen (Vendor adminPanel)
        //if user is buyer , start customer main screen (Customer Page)

        Task<DocumentSnapshot> ref = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String accountType = ""+documentSnapshot.getString("accountType");

                        if(accountType.equals("Seller")){
                            //user is seller
                            startActivity(new Intent(SplashActivity.this, MainVendorActivity.class));
                        }
                        else {
                            //user is buyer
                            startActivity(new Intent(SplashActivity.this, MainCustomerActivity.class));
                        }
                        finish();
                    }
                });
    }

}