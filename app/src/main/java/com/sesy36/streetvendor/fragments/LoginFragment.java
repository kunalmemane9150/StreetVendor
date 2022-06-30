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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sesy36.streetvendor.FragmentReplacerActivity;
import com.sesy36.streetvendor.MainCustomerActivity;
import com.sesy36.streetvendor.MainVendorActivity;
import com.sesy36.streetvendor.R;

import java.util.HashMap;
import java.util.Map;


public class LoginFragment extends Fragment {

    private EditText emailEt, passwordEt;
    private TextView forgotPassTv, signUpTv;
    private Button loginBtn;
    private ProgressBar progressBar;


    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;

    private FirebaseUser user;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        clickListener();
    }

    private void init(View view) {
        emailEt = view.findViewById(R.id.emailEt);
        passwordEt = view.findViewById(R.id.passwordEt);
        loginBtn = view.findViewById(R.id.loginBtn);
        signUpTv = view.findViewById(R.id.signUpTv);
        forgotPassTv = view.findViewById(R.id.forgotPassTv);
        progressBar = view.findViewById(R.id.progressBar);

        user = FirebaseAuth.getInstance().getCurrentUser();

        auth = FirebaseAuth.getInstance();

    }

    private void clickListener() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });


        signUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentReplacerActivity) getActivity()).setFragment(new CreateUserAccountFragment());
            }
        });

        forgotPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentReplacerActivity) getActivity()).setFragment(new ForgotPasswordFragment());

            }
        });

    }

    private void loginUser() {
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();

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
        if (password.isEmpty()) {
            passwordEt.setError("Password is required!");
            passwordEt.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordEt.setError("Enter valid password");
            passwordEt.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            FirebaseUser user = auth.getCurrentUser();

//                            checkUserType(user);
                            makeMeOnline();


                            if (!user.isEmailVerified()) {
                                Toast.makeText(getContext(), "Please verify your email", Toast.LENGTH_SHORT).show();
                            }
//                            sendUserToMainActivity();
                        } else {
                            String exception = "Error: " + task.getException().getMessage();
                            Toast.makeText(getContext(), exception, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);

                        }
                    }
                });
    }

    private void checkUserType(FirebaseUser user) {
        //if user is seller, start seller main screen (Seller adminPanel)
        //if user is buyer , start user main screen (User shopping Page)

        Task<DocumentSnapshot> ref = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String accountType = documentSnapshot.getString("accountType");

                        if (accountType.equals("Seller")) {

                            if (getActivity() == null) {
                                return;
                            }

                            progressBar.setVisibility(View.GONE);
                            //user is seller
                            startActivity(new Intent(getActivity().getApplicationContext(), MainVendorActivity.class));
                            getActivity().finish();
                        } else {

                            if (getActivity() == null) {
                                return;
                            }

                            progressBar.setVisibility(View.GONE);
                            //user is buyer
                            startActivity(new Intent(getActivity().getApplicationContext(), MainCustomerActivity.class));
                            getActivity().finish();
                        }
                    }
                });
    }


    private void makeMeOnline() {
        // after logging in make user online

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        boolean status = true;

        Map<String, Object> map = new HashMap<>();
        map.put("online", status);

        assert user != null;
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        checkUserType(user);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


}