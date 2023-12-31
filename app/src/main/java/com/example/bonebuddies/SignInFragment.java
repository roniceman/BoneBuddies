package com.example.bonebuddies;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class SignInFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private EditText email, password;
    private Button signInButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale("en"));
        res.updateConfiguration(conf, dm);

        FirebaseApp.initializeApp(requireActivity());
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.useAppLanguage();

        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        signInButton = view.findViewById(R.id.signIn);

        // Check if a user is already signed in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, navigate to the next screen
            Intent dogo = new Intent(requireActivity(), DogControl.class);
            startActivity(dogo);
            requireActivity().finish(); // Optional: Close the current activity
        }

        signInButton.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (isAdded()) {
                            if (task.isSuccessful()) {
                                // Successful login
                                Intent dogo = new Intent(requireActivity(), DogControl.class);
                                startActivity(dogo);
                                requireActivity().finish(); // Optional: Close the current activity
                            } else {
                                // Failed login
                                Exception exception = task.getException();
                                if (exception != null) {
                                    Log.e("SignInFragment", "signInWithEmailAndPassword failed", exception);
                                    Toast.makeText(requireContext(), "Authentication failed: " + exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        });

        return view;
    }
}
