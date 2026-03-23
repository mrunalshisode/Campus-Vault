package com.campusvault;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegister, tvForgotPassword;
    SignInButton btnGoogleSignIn;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    GoogleSignInClient googleSignInClient;

    static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        tvRegister       = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(
                R.id.tvForgotPassword);
        btnGoogleSignIn  = findViewById(
                R.id.btnGoogleSignIn);

        // Configure Google Sign-In
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(
                                R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn
                .getClient(this, gso);

        // Skip login if already logged in
        if (mAuth.getCurrentUser() != null) {
            fetchRoleAndRedirect(
                    mAuth.getCurrentUser().getUid());
            return;
        }

        btnLogin.setOnClickListener(
                v -> loginUser());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this,
                        RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(
                v -> showForgotPasswordDialog());

        btnGoogleSignIn.setOnClickListener(
                v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent =
                googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode,
                resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(
                            data);
            try {
                GoogleSignInAccount account =
                        task.getResult(
                                ApiException.class);
                firebaseAuthWithGoogle(
                        account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this,
                        "Google sign-in failed: "
                                + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(
            String idToken) {
        AuthCredential credential =
                GoogleAuthProvider
                        .getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user =
                            authResult.getUser();
                    String uid = user.getUid();

                    // Check if user exists in Firestore
                    db.collection("users")
                            .document(uid).get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()) {
                                    // New Google user —
                                    // save as student by default
                                    Map<String, Object> userData
                                            = new HashMap<>();
                                    userData.put("name",
                                            user.getDisplayName());
                                    userData.put("email",
                                            user.getEmail());
                                    userData.put("role",
                                            "student");

                                    db.collection("users")
                                            .document(uid)
                                            .set(userData)
                                            .addOnSuccessListener(
                                                    unused ->
                                                            fetchRoleAndRedirect(
                                                                    uid));
                                } else {
                                    fetchRoleAndRedirect(uid);
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Auth failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void loginUser() {
        String email    = etEmail.getText()
                .toString().trim();
        String password = etPassword.getText()
                .toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Enter email and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(
                        email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult
                            .getUser().getUid();
                    fetchRoleAndRedirect(uid);
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(this,
                            "Login failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showForgotPasswordDialog() {
        android.widget.EditText emailInput =
                new android.widget.EditText(this);
        emailInput.setHint("Enter your email");
        emailInput.setInputType(
                android.text.InputType
                        .TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setPadding(40, 20, 40, 20);

        String currentEmail = etEmail.getText()
                .toString().trim();
        if (!currentEmail.isEmpty()) {
            emailInput.setText(currentEmail);
        }

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage(
                        "Enter your registered email."
                                + " A reset link will be sent.")
                .setView(emailInput)
                .setPositiveButton("Send Reset Link",
                        (dialog, which) -> {
                            String email = emailInput
                                    .getText().toString()
                                    .trim();
                            if (email.isEmpty()) {
                                Toast.makeText(this,
                                        "Enter your email",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            sendPasswordReset(email);
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordReset(String email) {
        Toast.makeText(this,
                "Sending reset email...",
                Toast.LENGTH_SHORT).show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        new AlertDialog.Builder(this)
                                .setTitle("Email Sent! ✓")
                                .setMessage(
                                        "Reset link sent to:\n\n"
                                                + email
                                                + "\n\nCheck inbox"
                                                + " and spam folder.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        Exception e =
                                task.getException();
                        String error = e != null
                                ? e.getMessage()
                                : "Unknown error";
                        new AlertDialog.Builder(this)
                                .setTitle("Failed")
                                .setMessage("Error: " + error)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
    }

    private void fetchRoleAndRedirect(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role =
                                doc.getString("role");
                        Intent intent;

                        switch (role != null
                                ? role : "student") {
                            case "faculty":
                                intent = new Intent(this,
                                        FacultyDashboardActivity
                                                .class);
                                break;
                            case "admin":
                                intent = new Intent(this,
                                        AdminDashboardActivity
                                                .class);
                                break;
                            case "hod":
                                intent = new Intent(this,
                                        HodDashboardActivity
                                                .class);
                                break;
                            default:
                                intent = new Intent(this,
                                        StudentDashboardActivity
                                                .class);
                        }

                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Profile not found."
                                        + " Please register.",
                                Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        if (btnLogin != null)
                            btnLogin.setEnabled(true);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}