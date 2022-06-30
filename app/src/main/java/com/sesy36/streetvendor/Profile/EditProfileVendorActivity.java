package com.sesy36.streetvendor.Profile;

import static com.sesy36.streetvendor.fragments.CreateUserAccountFragment.EMAIL_REGEX;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sesy36.streetvendor.MainVendorActivity;
import com.sesy36.streetvendor.R;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileVendorActivity extends AppCompatActivity {

    private ImageButton backBtn, editProfileBtn;
    private CircleImageView profileImage;
    private EditText nameEt, shopNameEt, emailEt, phoneEt, stateEt, cityEt, addressEt;
    private Button updateBtn;
    private ProgressBar progressBar;


    // permission constants
    private  static final int CAMERA_REQUEST_CODE = 200;
    private  static final int STORAGE_REQUEST_CODE = 300;

    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //permission array
    private String [] cameraPermissions;
    private String [] storagePermissions;
    //image uri
    private Uri image_uri;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_vendor);

        init();
        onClickListener();
        loadUserInfo();

    }


    private void init(){

        backBtn = findViewById(R.id.backBtn);
        profileImage = findViewById(R.id.profileImage);
        nameEt = findViewById(R.id.nameEt);
        shopNameEt = findViewById(R.id.shopNameEt);
        emailEt = findViewById(R.id.emailEt);
        phoneEt = findViewById(R.id.phoneEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        updateBtn = findViewById(R.id.updateBtn);
        progressBar = findViewById(R.id.progressBar);
        editProfileBtn = findViewById(R.id.editProfileBtn);


        //initialize permission array
        cameraPermissions = new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void onClickListener() {

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

    }

    private void loadUserInfo() {

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

                        String userName = value.getString("name");
                        String shopName = value.getString("shopName");
                        String emailID = value.getString("email");
                        String phoneNo = value.getString("phoneNo");
                        String state = value.getString("state");
                        String city = value.getString("city");
                        String address = value.getString("address");
                        String profileURL = value.getString("profileImage");

                        nameEt.setText(userName);
                        shopNameEt.setText(shopName);
                        emailEt.setText(emailID);
                        phoneEt.setText(phoneNo);
                        stateEt.setText(state);
                        cityEt.setText(city);
                        addressEt.setText(address);

                        try {
                            Glide.with(EditProfileVendorActivity.this)
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

    private String name , shopName, email, phone, state, city, address;

    private void inputData() {
        //input data
        name = nameEt.getText().toString().trim();
        shopName = shopNameEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();

        if (email.isEmpty()) {
            emailEt.setError("Email is required!");
            emailEt.requestFocus();
            return;
        }
        if (!email.matches(EMAIL_REGEX)) {
            emailEt.setError("Enter valid email");
            emailEt.requestFocus();
            return;
        }

        updateProfile();
    }

    private void updateProfile() {

    progressBar.setVisibility(View.VISIBLE);


        if (image_uri == null){
            // update without image

            //setup data to update
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("shopName", shopName);
            map.put("email", email);
            map.put("phoneNo", phone);
            map.put("state", state);
            map.put("city", city);
            map.put("address", address);

            //update to db
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(user.getUid())
                    .update(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditProfileVendorActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditProfileVendorActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });

            //update email in authentication
            user.updateEmail(email)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(EditProfileVendorActivity.this, "Email is updated", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileVendorActivity.this, "Use your old email to sign in" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        else {
            //update with image
            /*----- Upload image first ------*/
            String filePathAndName = "profileImages/" + ""+ user.getUid();
            //get storage reference
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

            storageReference.putFile(image_uri)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            storageReference.getDownloadUrl()
                                    .addOnSuccessListener(uri1 -> {

                                        String imageURL = uri1.toString();

                                        //setup data to update
                                        Map<String, Object> map = new HashMap<>();

                                        map.put("name", name);
                                        map.put("shopName", shopName);
                                        map.put("email", email);
                                        map.put("phoneNo", phone);
                                        map.put("state", state);
                                        map.put("city", city);
                                        map.put("address", address);
                                        map.put("profileImage", imageURL);

                                        FirebaseFirestore.getInstance().collection("Users")
                                                .document(user.getUid())
                                                .update(map).addOnCompleteListener(task1 -> {

                                                    if (task1.isSuccessful()) {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(EditProfileVendorActivity.this, "Updated Successfully...", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        assert task1.getException() != null;
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(EditProfileVendorActivity.this, "Error: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }

                                                });
                                    });
                        } else {
                            assert task.getException() != null;
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditProfileVendorActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            //update email in authentication
            user.updateEmail(email)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(EditProfileVendorActivity.this, "Email is updated", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileVendorActivity.this, "Use your old email to sign in" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }


    //TODO----Image pick form gallery and camera & check permissions
    private void showImagePickDialog() {
        //options to display in dialog box
        String[] options = {"Camera","Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle iten click
                        if(which==0){
                            // camera clicked
                            if (checkCameraPermission()){
                                // allowed, open camera
                                pickFromCamera();
                            }
                            else {
                                //not allowed, request
                                requestCameraPermission();
                            }
                        }
                        else {
                            //gallery clicked
                            if (checkStoragePermission()){
                                // allowed, open gallery
                                pickFromGallery();
                            }
                            else {
                                //not allowed, request
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void pickFromCamera() {
        //intent to pick image form camera

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Image Description");

        image_uri =getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    private void pickFromGallery() {
        //intent to pick image form gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //permission allowded
                        pickFromCamera();
                    }else {
                        // permission denied
                        Toast.makeText(this, "Camera permissions are necessary..", Toast.LENGTH_SHORT).show();

                    }
                }
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission allowded
                        pickFromGallery();
                    }else {
                        // permission denied
                        Toast.makeText(this, "Storage permission is necessary..", Toast.LENGTH_SHORT).show();

                    }
                }
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        // handle image pick result
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                // pick image form gallery
                image_uri = data.getData();
                //set to image view
                profileImage.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                profileImage.setImageURI(image_uri);
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    //TODO----Image pick form gallery and camera & check permissions end





}