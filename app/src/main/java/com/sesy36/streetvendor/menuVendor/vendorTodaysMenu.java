package com.sesy36.streetvendor.menuVendor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sesy36.streetvendor.R;
import com.sesy36.streetvendor.adapter.veggieAdapter;
import com.sesy36.streetvendor.model.veggieItems;
import com.sesy36.streetvendor.recyclerItemSelectedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class vendorTodaysMenu extends AppCompatActivity implements recyclerItemSelectedListener, View.OnClickListener {

    ChipGroup itemsChips;
    private FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore fStore;
    RecyclerView itemList;
    veggieAdapter veggieAdapter;
    List<veggieItems> veggieItems = new ArrayList<>();
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_todays_menu);
        itemsChips = findViewById(R.id.itemChips);
        firebaseAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();

        itemList = findViewById(R.id.itemsList);
        itemList.setLayoutManager(new LinearLayoutManager(this));
        itemList.setHasFixedSize(true);
        getItems();
        veggieAdapter = new veggieAdapter(vendorTodaysMenu.this,veggieItems);
        itemList.setAdapter(veggieAdapter);
        DocumentReference docRef = fStore.collection("Users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.getData().get("todaysItems")!=null){
                            HashMap<String,Boolean> items =(HashMap<String, Boolean>) document.getData().get("todaysItems");
                            Log.d(TAG, "onComplete: "+ items);
                        }

                    }
                }
                else {}
            }
        });




    }


    public void getItems()
    {
        List<String> names = Arrays.asList(getResources().getStringArray(R.array.names));
        int [] imageIds = {R.drawable.banana,R.drawable.apple,R.drawable.tomato,R.drawable.onion,R.drawable.carrot,R.drawable.dragon_fruit};
        int count = 0;

        for(String name: names)
        {
            veggieItems.add(new veggieItems(name,imageIds[count]));
            count++;
        }
    }

    @Override
    public void onItemSelector(veggieItems veggieItems) {


        DocumentReference docRef = fStore.collection("Users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.getData().get("todaysItems")!=null){
                            HashMap<String,Boolean> items =(HashMap<String, Boolean>) document.getData().get("todaysItems");

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                if(items.get(veggieItems.getName()))
                                {}
                                else
                                {
                                    Chip chip = new Chip(vendorTodaysMenu.this);
                                    chip.setText(veggieItems.getName());
                                    chip.setChipIcon(ContextCompat.getDrawable(vendorTodaysMenu.this,veggieItems.getPicId()));
                                    chip.setCloseIconVisible(true);
                                    chip.setCheckable(false);
                                    chip.setClickable(false);
                                    chip.setOnCloseIconClickListener(vendorTodaysMenu.this);
                                    itemsChips.addView(chip);
                                    itemsChips.setVisibility(View.VISIBLE);
                                    items.replace(veggieItems.getName(),true);}

                            }
                            docRef.update("todaysItems",items );
                        }

                    }
                }
                else {}
            }
        });



    }

    @Override
    public void onClick(View view) {
        Chip chip = (Chip) view;
        itemsChips.removeView(chip);
        String itemName = (String) chip.getText();
        Log.d(TAG, "onClick: "+ itemName);
        DocumentReference docRef = fStore.collection("Users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.getData().get("todaysItems")!=null){
                            HashMap<String,Boolean> items =(HashMap<String, Boolean>) document.getData().get("todaysItems");

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                if(items.get(itemName))
                                {
                                    items.replace(itemName,false);
                                }
                                else
                                {
                                }
                            }
                            docRef.update("todaysItems",items );
                        }

                    }
                }
                else {}
            }
        });
//
    }
}