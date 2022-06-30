package com.sesy36.streetvendor;

//import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sesy36.streetvendor.Profile.EditProfileCustomerActivity;
import com.sesy36.streetvendor.Profile.EditProfileVendorActivity;
import com.sesy36.streetvendor.adapter.ShopsAdapter;
import com.sesy36.streetvendor.model.ShopModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainCustomerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    //drawer layout var
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView userNameTv;
    CircleImageView profileImage;

    private ImageButton drawerNav;
    private FirebaseAuth firebaseAuth;
    private RecyclerView vendorListRv;
    ShopsAdapter adapter;
    private ArrayList<ShopModel> list;


    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_customer_activity);

        init();
        onClickListener();
        loadShopDataFromFirestore();
        loadUserInfo();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                MainCustomerActivity.this, LinearLayoutManager.HORIZONTAL, false
        );
        vendorListRv.setLayoutManager(linearLayoutManager);
        vendorListRv.setItemAnimator(new DefaultItemAnimator());


    }


    private void init(){

        //navigation drawer layout ----
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        drawerNav = findViewById(R.id.drawerNav);
        userNameTv = findViewById(R.id.userNameTv);
        user = FirebaseAuth.getInstance().getCurrentUser();

        setSupportActionBar(toolbar);

        navigationView.bringToFront();

        navigationView.setNavigationItemSelectedListener(this);
        //navigation drawer end----


        vendorListRv = findViewById(R.id.vendorListRv);
        vendorListRv.setHasFixedSize(true);
        vendorListRv.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<ShopModel>();

        adapter = new ShopsAdapter(list, getApplicationContext());
        vendorListRv.setAdapter(adapter);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void onClickListener() {

        //drawer layout onclick
        drawerNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


    }

    private void loadShopDataFromFirestore() {

        FirebaseFirestore.getInstance().collection("Users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        try {
                            if (document.get("accountType").equals("Seller")) {
                                ShopModel vendordetails = new ShopModel(
                                        document.get("profileImage").toString(),
                                        document.get("shopName").toString(),
                                        document.get("uid").toString(),
                                        document.get("phoneNo").toString() );
                                list.add(vendordetails);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d("Failed", "Error getting documents: ", task.getException());
                }

            }

        });

    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainCustomerActivity.this, FragmentReplacerActivity.class));
            finish();
        }
    }

    //TODO---- load current user info
    @SuppressLint("SetTextI18n")
    private void loadUserInfo() {

        View drawerHeader =  navigationView.getHeaderView(0);

        userNameTv = drawerHeader.findViewById(R.id.userNameTv);
        profileImage = drawerHeader.findViewById(R.id.profileImage);

        FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid())
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        return;
                    }
                    if (value == null) {
                        return;
                    }
                    if (!value.exists()) {
                        return;
                    }

                    if (value.exists()){

                        Log.d("SUCCESSFUL customerName", value.getString("name"));

                        String userName = value.getString("name");

                        userNameTv.setText("Hey, "+ userName);
                        String profileURL = value.getString("profileImage");

                        try {
                            Glide.with(MainCustomerActivity.this)
                                    .load(profileURL)
                                    .placeholder(R.drawable.ic_person_dark)
                                    .timeout(6500)
                                    .into(profileImage);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                });

    }


    //TODO---- make user status offline
    private void makeMeOffline() {
        // after logging in make user online

        boolean status = false;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        Map<String, Object> map = new HashMap<>();
        map.put("online", status);

        assert user != null;
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainCustomerActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    //TODO---- navigation drawer ----
    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.navHome:
                onBackPressed();
                break;

            case R.id.navAllVendors:
                Toast.makeText(this, "Showing all Users..", Toast.LENGTH_SHORT).show();
                break;

            case R.id.editProfile:
                Intent intent = new Intent(MainCustomerActivity.this, EditProfileCustomerActivity.class);
                startActivity(intent);
                break;

            case R.id.navLogout:
                makeMeOffline();
                checkUser();
                break;

            case R.id.navShare:
                Toast.makeText(this, "Share ...", Toast.LENGTH_SHORT).show();
                break;

            case R.id.navRate:
                Toast.makeText(this, "Rate..", Toast.LENGTH_SHORT).show();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

}