package com.sesy36.streetvendor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sesy36.streetvendor.R;
import com.sesy36.streetvendor.menuVendor.vendorTodaysMenu;
import com.sesy36.streetvendor.model.veggieItems;
import com.sesy36.streetvendor.recyclerItemSelectedListener;

import java.util.List;

public class veggieAdapter extends RecyclerView.Adapter<veggieAdapter.myViewHolder> {
    Context context;
    private List<veggieItems> veggieItems;
    private recyclerItemSelectedListener itemSelectedListener;

    public veggieAdapter(Context context, List<veggieItems> veggieItems)
    {
        this.context=context;
        this.veggieItems=veggieItems;
        itemSelectedListener = (vendorTodaysMenu)context;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vendor_todays_item, parent, false);

        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        holder.itemName.setText(veggieItems.get(position).getName());
        holder.itemPhoto.setImageDrawable(ContextCompat.getDrawable(context, veggieItems.get(position).getPicId()));
    }

    @Override
    public int getItemCount() {
        return veggieItems.size();
    }


    class myViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView itemPhoto;
        TextView itemName;
        LinearLayout rootView;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            itemPhoto = itemView.findViewById(R.id.itemPhoto);
            itemName = itemView.findViewById(R.id.itemName);
            rootView = itemView.findViewById(R.id.rootView);


            rootView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemSelectedListener.onItemSelector(veggieItems.get(getAbsoluteAdapterPosition()));

        }
    }
}
