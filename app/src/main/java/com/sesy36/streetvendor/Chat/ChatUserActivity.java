package com.sesy36.streetvendor.Chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sesy36.streetvendor.R;
import com.sesy36.streetvendor.adapter.ChatUserAdapter;
import com.sesy36.streetvendor.model.ChatUserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatUserActivity extends AppCompatActivity {

    ChatUserAdapter adapter;
    List<ChatUserModel> list;
    FirebaseUser user;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user);

        init();

        fetchUserData();

        clickListener();

    }

    void init(){

        recyclerView = findViewById(R.id.recyclerView);
        list = new ArrayList<>();
        adapter = new ChatUserAdapter(this, list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();

    }

    private void clickListener() {
        adapter.OnStartChat((position, uid, chatID) -> {

            String oppositeUID;
            if (!uid.get(0).equalsIgnoreCase(user.getUid())) {
                oppositeUID = uid.get(0);
            } else {
                oppositeUID = uid.get(1);
            }

            Intent intent = new Intent(ChatUserActivity.this, ChatActivity.class);
            intent.putExtra("uid", oppositeUID);
            intent.putExtra("id", chatID);
            startActivity(intent);

        });
    }

    private void fetchUserData() {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
        reference.whereArrayContains("uid", user.getUid())
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        return;
                    }

                    if (value.isEmpty()) {
                        return;
                    }


                    list.clear();
                    for (QueryDocumentSnapshot snapshot : value) {

                        if (snapshot.exists()) {
                            ChatUserModel model = snapshot.toObject(ChatUserModel.class);
                            list.add(model);
                        }

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
