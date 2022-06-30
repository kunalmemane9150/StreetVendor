package com.sesy36.streetvendor.adapter;

import android.app.Activity;
import android.os.Build;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sesy36.streetvendor.R;
import com.sesy36.streetvendor.model.ChatUserModel;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ChatUserHolder> {

    public OnStartChat startChat;
    Activity context;
    List<ChatUserModel> list;

    public ChatUserAdapter(Activity context, List<ChatUserModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChatUserAdapter.ChatUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_user_item, parent, false);
        return new ChatUserHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ChatUserAdapter.ChatUserHolder holder, int position) {

        fetchImageUrl(list.get(position).getUid(), holder);

        holder.timeTv.setText(calculateTime(list.get(position).getTime()));
        holder.lastMessageTv.setText(list.get(position).getLastMessage());

        holder.itemView.setOnClickListener(v -> {

            startChat.clicked(position, list.get(position).getUid(), list.get(position).getId());

        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    String calculateTime(Date date){
        long millis = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            millis = date.toInstant().toEpochMilli();
        }

        return DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(), 6000, DateUtils.FORMAT_ABBREV_TIME).toString();

    }

    void fetchImageUrl(List<String> uid, ChatUserHolder holder){

        String oppositeUID;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        if(!uid.get(0).equalsIgnoreCase(user.getUid())){
            oppositeUID = uid.get(0);
        }else {
            oppositeUID = uid.get(1);
        }

        FirebaseFirestore.getInstance().collection("Users")
                .document(oppositeUID)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot snapshot = task.getResult();

                        Glide.with(context.getApplicationContext())
                                .load(snapshot.getString("profileImage"))
                                .placeholder(R.drawable.ic_person)
                                .into(holder.profileImage);

                        holder.nameTv.setText(snapshot.getString("name"));

                    }else {
                        assert  task.getException() != null;
                        Toast.makeText(context, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void OnStartChat(OnStartChat startChat){
        this.startChat = startChat;
    }

    public interface  OnStartChat{
        void clicked(int position, List<String> uid, String chatID );
    }

    public static class ChatUserHolder extends RecyclerView.ViewHolder {

        private TextView nameTv,lastMessageTv,timeTv, messageCountTv;
        private CircleImageView profileImage;

        public ChatUserHolder(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageCountTv = itemView.findViewById(R.id.messageCountTv);
            profileImage = itemView.findViewById(R.id.profileImage);


            messageCountTv.setVisibility(View.GONE);

        }
    }
}
