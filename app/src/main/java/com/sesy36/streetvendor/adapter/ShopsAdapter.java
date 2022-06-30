package com.sesy36.streetvendor.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sesy36.streetvendor.R;
import com.sesy36.streetvendor.ShopDetailsActivity;
import com.sesy36.streetvendor.model.ShopModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopsAdapter extends RecyclerView.Adapter<ShopsAdapter.ShopHolder>
{
    private ArrayList<ShopModel> shopsList ;
    Context context;

    public ShopsAdapter(ArrayList<ShopModel> shopsList, Context context) {
        this.shopsList = shopsList;
        this.context = context;
        Log.d("SUCCESSFUL", "HomeAdapter constructor");
    }

    @NonNull
    @Override
    public ShopsAdapter.ShopHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_shop, parent, false);
        return new ShopHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopsAdapter.ShopHolder holder, int position) {

        ShopModel shopModel = shopsList.get(position);
        String uid = shopModel.getUid();
        String shopName = shopModel.getShopName();
        String shopImage = shopModel.getProfileImage();
        String shopPhone = shopModel.getPhoneNo();


        //Set data
        holder.shopNameTv.setText(shopName);

        try {
            Picasso.get().load(shopImage).placeholder(R.drawable.ic_shopping).into(holder.shopImage);
        }
        catch (Exception e){
            holder.shopImage.setImageResource(R.drawable.ic_shopping);
        }

        //Handle click listener, show shop details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid", uid);
                intent.putExtra("shopName", shopName);
                intent.putExtra("shopPhone", shopPhone);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return shopsList.size();
    }

    public static class ShopHolder extends RecyclerView.ViewHolder{

        private ImageView shopImage;
        private TextView shopNameTv;

        public ShopHolder(@NonNull View itemView) {
            super(itemView);

            shopImage = itemView.findViewById(R.id.vendorImage);
            shopNameTv = itemView.findViewById(R.id.shopName);

            Log.d("SUCCESSFUL", "HomeHolder constructor");



        }
    }

}
