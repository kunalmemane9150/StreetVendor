package com.sesy36.streetvendor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.sesy36.streetvendor.Chat.ChatActivity;
import com.sesy36.streetvendor.Chat.ChatUserActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopDetailsActivity extends AppCompatActivity {

    private TextView shopNameTv, phoneNoTv, emailTv, addressTv, openStatusTv;
    private ImageButton callBtn, chatBtn, backBtn;
    private ImageView shopIc;
    private String shopUid, shopName, shopEmail, shopPhone, userUID;

    DocumentReference shopRef;

    private ProgressDialog progressDialog;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        init();
        onClickListener();
        loadShopDetails();

    }


    private void init() {
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneNoTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        callBtn = findViewById(R.id.callBtn);
        chatBtn = findViewById(R.id.chatBtn);
        backBtn = findViewById(R.id.backBtn);
        addressTv = findViewById(R.id.addressTv);
        openStatusTv = findViewById(R.id.openStatusTv);
        shopIc = findViewById(R.id.shopIc);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userUID = user.getUid();

        shopUid = getIntent().getStringExtra("shopUid");
        shopName = getIntent().getStringExtra("shopName");
        shopPhone = getIntent().getStringExtra("shopPhone");

        System.out.println("\n new user \n");
        Log.d("SUCCESSFUL shop name", shopName);
        System.out.println("\n");
        Log.d("SUCCESSFUL shop uid", shopUid);
        Log.d("SUCCESSFUL user uid", user.getUid());
        System.out.println("\n new user end\n");

        shopRef = FirebaseFirestore.getInstance().collection("Users")
                .document(shopUid);
    }

    private void onClickListener() {

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryChat();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
    }

    private void loadShopDetails() {
        shopRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value.exists()) {
                    String shopName = value.getString("shopName");
                    String shopPhone = value.getString("phoneNo");
                    String email = value.getString("email");
                    String address = value.getString("address");
                    String profileImage = value.getString("profileImage");
                    boolean isOnline = Boolean.TRUE.equals(value.getBoolean("status"));

                    shopNameTv.setText(shopName);
                    phoneNoTv.setText(shopPhone);
                    emailTv.setText(email);
                    addressTv.setText(address);

                    if (isOnline) {
                        openStatusTv.setTextColor(getResources().getColor(R.color.color_green_light));
                    } else {
                        openStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                    }
                    openStatusTv.setText(isOnline ? "Shop Open" : "Shop Closed");

                    try {
                        Picasso.get().load(profileImage).into(shopIc);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    //TODO---- chat feature
    void queryChat() {

        CollectionReference reference = FirebaseFirestore.getInstance()
                .collection("Messages");
        reference.whereEqualTo("uid", Arrays.asList(userUID, shopUid))
                .get().addOnCompleteListener(task -> {

//                  2.  reference.whereArrayContains("uid", userUID)
//                            .get().addOnCompleteListener(task -> {

//                  1.  reference.whereArrayContains("uid", shopUid)
//                            .get().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        QuerySnapshot snapshot = task.getResult();

                        if (snapshot.isEmpty()) {
                            progressDialog.setMessage("Staring Chat...");
                            progressDialog.show();
                            startChat(progressDialog);
                        } else {
                            //get chat id and pass
                            progressDialog.dismiss();
                            for (DocumentSnapshot snapshotChat : snapshot) {

                                Intent intent = new Intent(ShopDetailsActivity.this, ChatActivity.class);
                                intent.putExtra("uid", shopUid);
                                intent.putExtra("id", snapshotChat.getId());    //return doc id
                                startActivity(intent);
                            }


                        }

                    } else
                        progressDialog.dismiss();

                });
    }

    void startChat(ProgressDialog progressDialog) {


        CollectionReference reference = FirebaseFirestore.getInstance()
                .collection("Messages");

        List<String> list = new ArrayList<>();

        list.add(0, user.getUid());
        list.add(1, shopUid);

        String pushID = reference.document().getId();


        Map<String, Object> map = new HashMap<>();
        map.put("id", pushID);
        map.put("lastMessage", "");
        map.put("time", FieldValue.serverTimestamp());
        map.put("uid", list);

        reference.document(pushID).update(map).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                reference.document(pushID).set(map);
            }
        });

        //todo ---
        //messages
        CollectionReference messageRef = FirebaseFirestore.getInstance()
                .collection("Messages")
                .document(pushID)
                .collection("Messages");

        String messageID = messageRef.document().getId();

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("id", messageID);
        messageMap.put("message", null);
        messageMap.put("senderID", user.getUid());
        messageMap.put("time", FieldValue.serverTimestamp());

        messageRef.document(messageID).set(messageMap);

        new Handler().postDelayed(() -> {

            progressDialog.dismiss();

            Intent intent = new Intent(ShopDetailsActivity.this, ChatActivity.class);
            intent.putExtra("uid", shopUid);
            intent.putExtra("id", pushID);
            startActivity(intent);

        }, 2000);
    }
    //TODO---- chat feature end

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

}