package com.example.apprenti.twitter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
public class LoginActivity extends AppCompatActivity {
    final String userName = "NameKey";
    final String userPassword = "PasswordKey";
    private boolean auth = false;
    private String mUserId = "UserKey";
    private String mEncrypt = "encrypt";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText editTextUserName = findViewById(R.id.connexionUserName);
        final EditText editTextUserPassword = findViewById(R.id.connexionUserPassword);
        Button buttonLog = findViewById(R.id.buttonConnexionSend);
        Button buttonGoCreate = findViewById(R.id.buttonGoCreerCompt);
        // On recupere les Shared  Preferences
        final SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String sharedPrefUserName = sharedpreferences.getString(userName, "");
        final String sharedPrefUserPassword = sharedpreferences.getString(userPassword, "");
        final String sharedPrefUserKey = sharedpreferences.getString(mUserId, "");
        final ProgressBar simpleProgressBar = findViewById(R.id.simpleProgressBar);
        //On rempli les editText avec les sharedPreferences si c'est pas notre premiere connexion
        if (!sharedPrefUserName.isEmpty() && !sharedPrefUserPassword.isEmpty()) {
            editTextUserName.setText(sharedPrefUserName);
            editTextUserPassword.setText(sharedPrefUserPassword);
        }
        // Au clic du bouton, c'est la que tout se passe !!!!!!!!
        buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                simpleProgressBar.setVisibility(View.VISIBLE);
                //On recupere le contenu des edit text
                final String userNameContent = editTextUserName.getText().toString();
                final String userPasswordContent = editTextUserPassword.getText().toString();
                // Snackbar si les champs ne sont pas remplis
                if (TextUtils.isEmpty(userNameContent) || TextUtils.isEmpty(userPasswordContent)) {
                    Snackbar.make(view, "Veuillez remplir tous les champs", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    simpleProgressBar.setVisibility(view.GONE);
                } else {
                    // Sinon on recupere tous les users
                    final DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("User");
                    refUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                                User userValues = dsp.getValue(User.class);
                                //On compare le contenu des edit text avec Firebase grâce au user_name
                                if (userValues.getUser_name().equals(userNameContent)) {
                                    // On verifie le password
                                    if (userValues.getUser_password().equals(mEncrypt(userPasswordContent, "AES"))) {
                                        // La clé de l'utilisateur qu'on va utiliser partout dans l'application.
                                        mUserId = dsp.getKey();
                                        // On sauvegarde l'utilisateur connu dans les sharedPreferences
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString(userName, userNameContent);
                                        editor.putString(userPassword, userPasswordContent);
                                        editor.apply();
                                        // If user is known : if he has no quest => LobbyActivity; if he has => PlayerActivity
                                        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(intent);
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    } else {
                                        // Si le mot de passe ou le pseudo ne concordent pas
                                        Snackbar.make(view, "Pseudo ou mot de passe invalide", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                        simpleProgressBar.setVisibility(View.GONE);
                                    }


                                }
                            }
                        }
                        // Encryptage du mot de passe
                        public String mEncrypt(String userPassword, String key) {
                            try {
                                Key clef = new SecretKeySpec(key.getBytes("ISO-8859-2"), "Blowfish");
                                Cipher cipher = Cipher.getInstance("Blowfish");
                                cipher.init(Cipher.ENCRYPT_MODE, clef);
                                return new String(cipher.doFinal(userPassword.getBytes()));
                            } catch (Exception e) {
                                return null;
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                    simpleProgressBar.setVisibility(view.GONE);
                }
            }
        });
        buttonGoCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,
                        CreateAccountActivity.class);
                startActivity(intent);

            }
        });
    }
}