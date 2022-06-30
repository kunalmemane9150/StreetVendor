package com.sesy36.streetvendor.fragments;

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
import com.sesy36.streetvendor.MainCustomerActivity;
import com.sesy36.streetvendor.R;

import java.util.HashMap;
import java.util.Map;


public class CreateUserAccountFragment extends Fragment {

    private EditText nameEt, emailEt, passwordEt, confirmPassEt;
    private TextView loginTv, vendorRegisterTv;
    private Button signUpBtn;
    private ImageView backBtn;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    FirebaseFirestore firebaseFirestore;

    public static final String EMAIL_REGEX = "^(.+)@(.+)$";   //"^(.+)@(.+)$"   '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,6}$'

    public CreateUserAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_user_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        clickListener();

        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    private void init(View view){
        nameEt = view.findViewById(R.id.nameEt);
        emailEt = view.findViewById(R.id.emailEt);
        passwordEt = view.findViewById(R.id.passwordEt);
        confirmPassEt = view.findViewById(R.id.confirmPassEt);
        loginTv = view.findViewById(R.id.loginTv);
        signUpBtn = view.findViewById(R.id.signUpBtn);
        progressBar = view.findViewById(R.id.progressBar);
        backBtn = view.findViewById(R.id.backBtn);
        vendorRegisterTv = view.findViewById(R.id.vendorRegisterTv);

        auth = FirebaseAuth.getInstance();
    }


    private void clickListener(){
        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentReplacerActivity) getActivity()).setFragment(new LoginFragment());
            }
        });

        vendorRegisterTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentReplacerActivity) getActivity()).setFragment(new CreateVendorAccountFragment());
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentReplacerActivity) getActivity()).setFragment(new LoginFragment());
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = nameEt.getText().toString().trim();
                String email = emailEt.getText().toString().trim();
                String password = passwordEt.getText().toString().trim();
                String confirmPassword = confirmPassEt.getText().toString().trim();

                if (name.isEmpty() || name.equals(" ")){
                    nameEt.setError("Please enter valid name");
                    nameEt.requestFocus();
                    return;
                }
                if (email.isEmpty() || !email.matches(EMAIL_REGEX)){
                    emailEt.setError("Please enter valid email");
                    emailEt.requestFocus();
                    return;
                }
                if (password.isEmpty()){
                    passwordEt.setError("Set password");
                    passwordEt.requestFocus();
                    return;
                }
                if(password.length() < 6){
                    passwordEt.setError("Enter valid password");
                    passwordEt.requestFocus();
                    return;
                }if (confirmPassword.isEmpty() || !confirmPassword.equals(password)){
                    confirmPassEt.setError("Password doesn't match");
                    confirmPassEt.requestFocus();
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);

                createAccount(name, email, password);



            }

        });
    }

    private void createAccount(String name, String email, String password){
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
                            uploadUser(user, name, email);

                        }else {
                            progressBar.setVisibility(View.GONE);
                            String exception = task.getException().getMessage();
                            Toast.makeText(getContext(), "Error: "+ exception, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadUser(FirebaseUser user, String name, String email){
        Map<String, Object> map = new HashMap<>();

        map.put("name", name);
        map.put("email", email);
        map.put("phoneNo", "");
        map.put("state", "");
        map.put("city", "");
        map.put("address", "");
        map.put("accountType","Customer");
        map.put("profileImage", "");
        map.put("uid", user.getUid());

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            assert getActivity() != null;
                            progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(getActivity().getApplicationContext(), MainCustomerActivity.class));
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