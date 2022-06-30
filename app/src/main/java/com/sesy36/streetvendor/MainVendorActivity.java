package com.sesy36.streetvendor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sesy36.streetvendor.Chat.ChatActivity;
import com.sesy36.streetvendor.Chat.ChatUserActivity;
import com.sesy36.streetvendor.Profile.EditProfileVendorActivity;
import com.sesy36.streetvendor.fragments.LoginFragment;
import com.sesy36.streetvendor.menuVendor.vendorTodaysMenu;
import com.sesy36.streetvendor.model.ChatModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainVendorActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //drawer layout var
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView userNameTv, shopOpenStatusTv, toolbarNameTv;
    SwitchCompat shopOpenSwitch;

    //today menu
    private Button todayMenuSelectBtn;

    CircleImageView profileImage;

    private ImageButton sendBtn, drawerNav;
    private FirebaseAuth firebaseAuth;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_vendor_activit);

        init();
        loadUserInfo();
        onClickListener();

        Log.d("SUCCESSFUL user uid", user.getUid());


    }


    private void init() {

        //navigation drawer layout ----
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        drawerNav = findViewById(R.id.drawerNav);
        toolbarNameTv = findViewById(R.id.toolbarNameTv);

        //today menu
        todayMenuSelectBtn = findViewById(R.id.todayMenuSelectBtn);

        user = FirebaseAuth.getInstance().getCurrentUser();

        setSupportActionBar(toolbar);

        navigationView.bringToFront();

        navigationView.setNavigationItemSelectedListener(this);

//        navigationView.setCheckedItem(R.id.navHome);
        //navigation drawer end----

        sendBtn = findViewById(R.id.sendBtn);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void onClickListener() {

        //TODO---- drawer layout onclick
        drawerNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //TODO---- chat btn onclick
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainVendorActivity.this, ChatUserActivity.class);
                startActivity(intent);
            }
        });


        //TODO---- change shop status as open or closed
        shopOpenSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shopSwitch();
            }
        });


        //TODO---- today menu
        todayMenuSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainVendorActivity.this, vendorTodaysMenu.class));
            }
        });

    }

    private void makeMeOffline() {
        // after logging in make user online

        boolean status = false;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        Map<String, Object> map = new HashMap<>();
        map.put("online", status);
        map.put("status", status);

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
                        Toast.makeText(MainVendorActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(MainVendorActivity.this, FragmentReplacerActivity.class));
            finish();
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadUserInfo() {

        View drawerHeader =  navigationView.getHeaderView(0);
        // shop open / close switch
        shopOpenStatusTv = drawerHeader.findViewById(R.id.shopOpenStatusTv);
        shopOpenSwitch = drawerHeader.findViewById(R.id.shopOpenSwitch);
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

                    Log.d("SUCCESSFUL ShopName", value.getString("shopName"));
                    Log.d("SUCCESSFUL status", String.valueOf(value.getBoolean("status")));

                        String userName = value.getString("shopName");
                        boolean isOnline = Boolean.TRUE.equals(value.getBoolean("status"));

                        userNameTv.setText("Hey, "+ userName);
                        String profileURL = value.getString("profileImage");

                        if (isOnline){
                            shopOpenStatusTv.setTextColor(getResources().getColor(R.color.color_green_light));
                        }else {
                            shopOpenStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                        }
                        shopOpenStatusTv.setText(isOnline ? "Shop Open" : "Shop Closed");

                        if (isOnline){
                            shopOpenSwitch.setChecked(true);
                        }else {
                            shopOpenSwitch.setChecked(false);
                        }

                        try {
                            Glide.with(MainVendorActivity.this)
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

    //TODO ----shop status function
    private void shopSwitch(){
        boolean shopOpen = shopOpenSwitch.isChecked(); //true or false

        Map<String, Object> map = new HashMap<>();
        map.put("status", shopOpen);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainVendorActivity.this, "Shop status updated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainVendorActivity.this, "Failed to open shop", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //TODO---- navigation drawer start ----
    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.navHome:
                onBackPressed();
                break;

            case R.id.navAllProducts:
                Toast.makeText(this, "Showing all Products..", Toast.LENGTH_SHORT).show();
                break;

            case R.id.editProfile:
                Intent intent = new Intent(MainVendorActivity.this, EditProfileVendorActivity.class);
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

    //TODO---- drawer layout end ----

}