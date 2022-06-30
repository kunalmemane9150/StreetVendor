package com.sesy36.streetvendor.fragments;

import static com.sesy36.streetvendor.fragments.CreateUserAccountFragment.EMAIL_REGEX;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sesy36.streetvendor.FragmentReplacerActivity;
import com.sesy36.streetvendor.R;
import com.sesy36.streetvendor.MainVendorActivity;

import java.util.HashMap;
import java.util.Map;


public class CreateVendorAccountFragment extends Fragment {

    private ImageView backBtn;
    private TextView loginTv;
    private EditText nameEt, shopNameEt, emailEt, phoneEt, stateEt, cityEt, addressEt,passwordEt, confirmPassEt;
    private Button signUpBtn;
    private ProgressBar progressBar;



    FirebaseAuth auth = FirebaseAuth.getInstance();

    FirebaseFirestore firebaseFirestore;

    public CreateVendorAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_vendor_account, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        clickListener();

        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    private void init(View view) {

        backBtn = view.findViewById(R.id.backBtn);
        loginTv = view.findViewById(R.id.loginTv);
        nameEt = view.findViewById(R.id.nameEt);
        shopNameEt = view.findViewById(R.id.shopNameEt);
        emailEt = view.findViewById(R.id.emailEt);
        phoneEt = view.findViewById(R.id.phoneEt);
        stateEt = view.findViewById(R.id.stateEt);
        cityEt = view.findViewById(R.id.cityEt);
        addressEt = view.findViewById(R.id.addressEt);
        passwordEt = view.findViewById(R.id.passwordEt);
        confirmPassEt = view.findViewById(R.id.confirmPassEt);
        signUpBtn = view.findViewById(R.id.signUpBtn);
        progressBar = view.findViewById(R.id.progressBar);

    }

    private void clickListener(){

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentReplacerActivity) getActivity()).setFragment(new LoginFragment());
            }
        });

        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentReplacerActivity) getActivity()).setFragment(new LoginFragment());
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                inputData();

            }
        });

    }

    private void inputData(){
        String name = nameEt.getText().toString().trim();
        String shopName = shopNameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String phoneNo = phoneEt.getText().toString().trim();
        String state = stateEt.getText().toString().trim();
        String city = cityEt.getText().toString().trim();
        String address = addressEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();
        String confirmPassword = confirmPassEt.getText().toString().trim();

        if (name.isEmpty() || name.equals(" ")){
            nameEt.setError("Please enter valid name");
            nameEt.requestFocus();
            return;
        }
        if (shopName.isEmpty() || shopName.equals(" ")){
            shopNameEt.setError("Please enter valid shop name");
            shopNameEt.requestFocus();
            return;
        }
        if (email.isEmpty() || !email.matches(EMAIL_REGEX)){
            emailEt.setError("Please enter valid email");
            emailEt.requestFocus();
            return;
        }
        String phoneNu= ".{10,}";
        if (!phoneNo.matches(phoneNu) || phoneNo.equals(" ")){
            phoneEt.setError("Enter valid phone number !");
            phoneEt.requestFocus();
            return;
        }
        if(state.isEmpty()){
            stateEt.setError("Enter state");
            stateEt.requestFocus();
            return;
        }
        if(city.isEmpty()){
            cityEt.setError("Enter city");
            cityEt.requestFocus();
            return;
        }
        if(address.isEmpty()){
            addressEt.setError("Enter address");
            addressEt.requestFocus();
            return;
        }
        if(password.isEmpty()){
            passwordEt.setError("Set Password");
            passwordEt.requestFocus();
            return;
        }

        String checkPass= "^"+
                //"(?=.*[0-9])" +           //at lest 1 digit
                //"(?=.*[a-z])" +           //at lest 1 lower caese letter
                //"(?=.*[A-Z])" +           //at lest 1 upper case letter
                "(?=.*[a-zA-z])" +          //any letter
                "(?=.*[@#$%^&+=])" +        //at lest 1 special character
                "(?=.*\\S+$)" +             //no white spaces
                ".{6,}" +                   //at lest 6 characters
                "$";
        if(!password.matches(checkPass)){
            passwordEt.setError("Set strong password Containing at least 6 characters.");
            passwordEt.requestFocus();
            return ;
        }
        if (confirmPassword.isEmpty() || !password.equals(confirmPassword)){
            confirmPassEt.setError("Password doesn't match");
            return;
        }

        createAccount(name,shopName, phoneNo, state, city, address, email, password);

    }

    private String name,shopName, phoneNo, state, city, address, email, password, confirmPassword;

    private void createAccount(String name, String shopName, String phoneNo, String state, String city, String address, String email, String password){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            FirebaseUser user =  auth.getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(getContext(), "Email verification link sent on your register email.", Toast.LENGTH_SHORT).show();
                                            }else{

                                            }
                                        }
                                    });
                            uploadUser(user, name,shopName, phoneNo, state, city, address, email);

                        }else {
                            progressBar.setVisibility(View.GONE);
                            String exception = task.getException().getMessage();
                            Toast.makeText(getContext(), "Error: "+ exception, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadUser(FirebaseUser user, String name, String shopName, String phoneNo, String state, String city, String address, String email){
        Map<String, Object> map = new HashMap<>();

        map.put("name", name);
        map.put("shopName", shopName);
        map.put("email", email);
        map.put("phoneNo", phoneNo);
        map.put("state", state);
        map.put("city", city);
        map.put("address", address);
        map.put("profileImage", " ");
        map.put("accountType","Seller");
        map.put("uid", user.getUid());

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            assert getActivity() != null;
                            progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(getActivity().getApplicationContext(), MainVendorActivity.class));
                            getActivity().finish();
                        }else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error: "+ task.getException().getMessage(), Toast.LENGTH_LONG).show();

                            emailEt.setError("Email is already registered, Please Sign In!");
                            return;
                        }
                    }
                });
    }

}