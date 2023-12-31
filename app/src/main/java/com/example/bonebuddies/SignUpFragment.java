package com.example.bonebuddies;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

public class SignUpFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 12345;
    private static final int CAMERA_PIC_REQUEST = 1337;
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText;
    private ImageButton profileImageButton;
    private Button signUpButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private ProgressDialog progressDialog;

    // Declare file-related variables at the class level
    private String filePath;
    private Uri fileUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale("en"));
        res.updateConfiguration(conf, dm);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        // Initialize views
        firstNameEditText = view.findViewById(R.id.firstName);
        lastNameEditText = view.findViewById(R.id.lastName);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        profileImageButton = view.findViewById(R.id.profileImageButton);
        signUpButton = view.findViewById(R.id.signUp);

        progressDialog = new ProgressDialog(requireContext());

        profileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickerDialog();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        return view;
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Image Source");
        builder.setItems(new CharSequence[]{"Gallery", "Camera"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        checkGalleryPermissionAndOpen();
                        break;
                    case 1:
                        checkCameraPermissionAndOpen();
                        break;
                }
            }
        });
        builder.show();
    }

    private void checkGalleryPermissionAndOpen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    private void signUp() {
        if (FirebaseApp.getApps(requireContext()).isEmpty()) {
            Toast.makeText(requireContext(), "Firebase not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || fileUri == null) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Signing up...");
        progressDialog.show();

        // Use the original file path obtained from the content URI
        filePath = getPathFromURI(fileUri);

        // Check if the file exists before proceeding with the upload
        File file = new File(filePath);
        if (!file.exists()) {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "Selected file does not exist", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use the original file path for the upload
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(email + ".jpg");

        try {
            InputStream stream = requireContext().getContentResolver().openInputStream(fileUri);
            UploadTask uploadTask = storageReference.putStream(stream);

            uploadTask.addOnCompleteListener(requireActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                createUserWithEmailAndPassword(email, password, firstName, lastName, imageUrl);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Log.e("UploadError", "Failed to upload image: " + e.getMessage());
                                Toast.makeText(requireContext(), "Failed to upload image" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (FileNotFoundException e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(requireContext(), "File not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createUserWithEmailAndPassword(final String email, String password, final String firstName, final String lastName, final String imageUrl) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Sign up success, update UI with the signed-in user's information
                            Toast.makeText(requireContext(), "Sign up successful", Toast.LENGTH_SHORT).show();
                            Intent dogo = new Intent(requireActivity(), DogControl.class);
                            startActivity(dogo);

                            saveUserDataToFirestore(email, firstName, lastName, imageUrl);
                        } else {
                            // If sign up fails, display a message to the user.
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthWeakPasswordException e) {
                                Toast.makeText(requireContext(), "Weak password", Toast.LENGTH_SHORT).show();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(requireContext(), "Invalid email", Toast.LENGTH_SHORT).show();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Toast.makeText(requireContext(), "User with this email already exists", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(requireContext(), "Sign up failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    // Function to sign in a user with email and password
    private void signInWithEmailAndPassword(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User is signed in, proceed with Firestore write.
                            String firstName = "UserFirstName";  // Replace with the actual first name
                            String lastName = "UserLastName";    // Replace with the actual last name
                            String imageUrl = "https://example.com/image.jpg";  // Replace with the actual image URL
                            saveUserDataToFirestore(email, firstName, lastName, imageUrl);
                        } else {
                            // Handle sign-in failure.
                            Toast.makeText(requireContext(), "Sign-in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Function to save user data to Firestore
    private void saveUserDataToFirestore(String email, String firstName, String lastName, String imageUrl) {
        // Create a User object with the provided data
        User user = new User(email, firstName, lastName, imageUrl);

        // Assume you have a "users" collection in Firestore
        firestore.collection("users")
                .document(email)  // Use a unique identifier, such as the user's email, as the document ID
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // User data saved successfully
                        Toast.makeText(requireContext(), "User data saved successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error saving user data
                        Toast.makeText(requireContext(), "Error saving user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to generate a unique document ID based on the user's email
    private String generateDocumentId(String email) {
        return email.replace(".", "_");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                if (data != null && data.getData() != null) {
                    fileUri = data.getData();
                    profileImageButton.setImageURI(fileUri);
                }
            } else if (requestCode == CAMERA_PIC_REQUEST) {
                if (data != null && data.getExtras() != null) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    // Save the bitmap to a file and get the file URI
                    fileUri = saveBitmapToFile(photo);
                    profileImageButton.setImageBitmap(photo);
                }
            }
        }
    }

    // Add this method to get the original file path from the content URI
    private String getPathFromURI(Uri contentUri) {
        if (contentUri.getScheme() != null && contentUri.getScheme().equals("content")) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = requireActivity().getContentResolver().query(contentUri, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                return filePath;
            } else {
                return null;
            }
        } else if (contentUri.getScheme() != null && contentUri.getScheme().equals("file")) {
            return contentUri.getPath();  // Directly return the file path
        } else {
            return null;
        }
    }

    // Add this method to save the bitmap to a file and get the file URI
    private Uri saveBitmapToFile(Bitmap bitmap) {
        // Save the bitmap to a temporary file
        File tempFile = new File(requireContext().getCacheDir(), "temp_image.jpg");
        try {
            tempFile.createNewFile();
            // Write the bitmap to the file
            tempFile = FileUtil.saveBitmapToFile(bitmap, tempFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the file URI
        return Uri.fromFile(tempFile);
    }
}

