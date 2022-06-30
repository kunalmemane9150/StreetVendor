package com.sesy36.streetvendor.Chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sesy36.streetvendor.R;
import com.sesy36.streetvendor.adapter.ChatAdapter;
import com.sesy36.streetvendor.model.ChatModel;
import com.sesy36.streetvendor.model.ShopModel;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    FirebaseUser user;
    CircleImageView profileImage;
    TextView shopNameTv, statusTv;
    EditText chatEt;
    ImageButton sendBtn;
    RecyclerView recyclerView;

    ChatAdapter adapter;
    List<ChatModel> list;

    String chatID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();

        loadUserData();

        loadMessages();

        onClickListener();
    }


    void init() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        profileImage = findViewById(R.id.profileImage);
        shopNameTv = findViewById(R.id.shopNameTv);
        statusTv = findViewById(R.id.statusTv);
        chatEt = findViewById(R.id.chatEt);
        sendBtn = findViewById(R.id.sendBtn);

        recyclerView = findViewById(R.id.recyclerView);

        list = new ArrayList<>();
        adapter = new ChatAdapter(this, list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void onClickListener() {

        sendBtn.setOnClickListener(v -> {

            String message = chatEt.getText().toString().trim();

            if (message.isEmpty()){
                return;
            }


            CollectionReference reference = FirebaseFirestore.getInstance()
                    .collection("Messages");


            Map<String, Object> map = new HashMap<>();

            map.put("lastMessage", message);
            map.put("time", FieldValue.serverTimestamp());

            reference.document(chatID).update(map);

            String messageID = reference
                    .document(chatID)
                    .collection("Messages")
                    .document()
                    .getId();

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", messageID);
            messageMap.put("message", message);
            messageMap.put("senderID", user.getUid());
            messageMap.put("time", FieldValue.serverTimestamp());

            reference.document(chatID).collection("Messages")
                    .document(messageID).set(messageMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            chatEt.setText("");
                        }else {
                            Toast.makeText(ChatActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });

        });
    }


    void loadUserData() {

        String oppositeUID = getIntent().getStringExtra("uid");

        FirebaseFirestore.getInstance().collection("Users")
                .document(oppositeUID)
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

                    boolean isOnline = Boolean.TRUE.equals(value.getBoolean("status"));
                    if (isOnline){
                        statusTv.setTextColor(getResources().getColor(R.color.color_green_light));
                    }else {
                        statusTv.setTextColor(getResources().getColor(R.color.text_color2));
                    }
                    statusTv.setText(isOnline ? "Online" : "Offline");

                    Glide.with(getApplicationContext())
                            .load(value.getString("profileImage"))
                            .placeholder(R.drawable.ic_person)
                            .into(profileImage);

                    if (value.get("accountType").equals("Seller")) {
                        shopNameTv.setText(value.getString("shopName"));
                    }else {
                        shopNameTv.setText(value.getString("name"));
                    }
                });
    }

    void loadMessages() {

        chatID = getIntent().getStringExtra("id");

        CollectionReference reference = FirebaseFirestore.getInstance()
                .collection("Messages")
                .document(chatID)
                .collection("Messages");

        reference.orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        return;
                    }

                    list.clear();
                    for (QueryDocumentSnapshot snapshot : value) {
                        ChatModel model = snapshot.toObject(ChatModel.class);
                        list.add(model);
                    }

                    adapter.notifyDataSetChanged();

                });

    }

    @Override
    protected void onResume() {
        updateStatus(true);
        super.onResume();
    }
    @Override
    protected void onPause() {
        updateStatus(false);
        super.onPause();

    }

    void updateStatus(boolean status){

        Map<String, Object> map = new HashMap<>();
        map.put("online",status);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .update(map);
    }

}