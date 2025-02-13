package com.newpackage.xxx;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import android.widget.Spinner;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.content.SharedPreferences;
import android.widget.CheckBox;



public class LoginActivity extends AppCompatActivity {
    private boolean isOrderPlaced = false;

    EditText usernameEditText, passwordEditText;

    private CheckBox rememberMeCheckBox;

    Button loginButton, orderAccount;

    private String serverUrl;

    private static final String TAG = "FCMToken";
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        setContentView(R.layout.activity_login);

        VideoView videoView = findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.backgroundvid);
        videoView.setVideoURI(videoUri);

        videoView.setVisibility(View.VISIBLE);
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            videoView.start();
            mp.setVolume(0f, 0f);
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                Log.d(TAG, "FCM Token: " + token);
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
            }
        });

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        orderAccount = findViewById(R.id.orderAccount);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        // SharedPreferences - Muista minut
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "");
        String savedPassword = sharedPreferences.getString("password", "");
        boolean isRemembered = sharedPreferences.getBoolean("rememberMe", false);

        if (isRemembered) {
            usernameEditText.setText(savedUsername);
            passwordEditText.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }

        serverUrl = getString(R.string.server_url);

        orderAccount.setOnClickListener(v -> {
            if (isOrderPlaced) {
                Toast.makeText(getApplicationContext(),
                        "Olet jo pyytänyt käyttäjätilin luomista. Jos tämä on virhe, ota yhteyttä asiakastukeen.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_order_account, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setView(dialogView);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            if (alertDialog.getWindow() != null) {
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_background);
            }

            Button cancelButton = dialogView.findViewById(R.id.cancelButton);
            Button saveButton = dialogView.findViewById(R.id.saveButton);

            EditText knimiEditText = dialogView.findViewById(R.id.knimiEditText);
            EditText tunnusEditText = dialogView.findViewById(R.id.tunnusEditText);
            EditText osoiteEditText = dialogView.findViewById(R.id.osoiteEditText);
            EditText postiEditText = dialogView.findViewById(R.id.postiEditText);
            EditText cityEditText = dialogView.findViewById(R.id.cityEditText);
            EditText emailEditText = dialogView.findViewById(R.id.emailEditText);
            EditText pnumeroEditText = dialogView.findViewById(R.id.pnumeroEditText);
            Spinner quantityTypeSpinner = dialogView.findViewById(R.id.quantityTypeSpinner);

            cancelButton.setOnClickListener(view -> alertDialog.dismiss());

            saveButton.setOnClickListener(view -> {
                String knimi = knimiEditText.getText().toString().trim();
                String pnumero = pnumeroEditText.getText().toString().trim();
                String tunnus = tunnusEditText.getText().toString().trim();
                String osoite = osoiteEditText.getText().toString().trim();
                String posti = postiEditText.getText().toString().trim();
                String city = cityEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String quantityType = quantityTypeSpinner.getSelectedItem().toString();

                String emailPattern = "^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$";
                String contactNumberPattern = "^\\+?[0-9]{10,13}$";
                String postalCodePattern = "^[0-9]{5}$";

                if (knimi.isEmpty() || tunnus.isEmpty() || osoite.isEmpty() || posti.isEmpty() || city.isEmpty() || email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Ole hyvä ja anna tarvittavat tiedot.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pnumero.matches(contactNumberPattern)) {
                    Toast.makeText(LoginActivity.this, "Ole hyvä ja tarkista puhelinumero.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!email.matches(emailPattern)) {
                    Toast.makeText(LoginActivity.this, "Ole hyvä ja tarkista sähköpostiosoite.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!posti.matches(postalCodePattern)) {
                    Toast.makeText(LoginActivity.this, "Ole hyvä ja tarkista postinumero.", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendOrderDetailsByEmail(knimi, tunnus, osoite, posti, city, email, pnumero, quantityType);

                Toast.makeText(LoginActivity.this, "Kiitos tiedoista!\nTarkistamme tiedot ja lähetämme tunnukset sähköpostitse mahdollisimman pian.", Toast.LENGTH_LONG).show();

                isOrderPlaced = true;

                alertDialog.dismiss();
            });
        });

        // Kirjautumisnappulan kuuntelija
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            new Thread(() -> {
                String result = performLogin(serverUrl, username, password);

                runOnUiThread(() -> {
                    handleLoginResult(result);

                    // Tallenna kirjautumistiedot, jos "Muista minut" on valittu
                    if (rememberMeCheckBox.isChecked()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", username);
                        editor.putString("password", password);
                        editor.putBoolean("rememberMe", true);
                        editor.apply();
                    } else {
                        // Tyhjennä tallennetut tiedot, jos "Muista minut" ei ole valittu
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                    }
                });
            }).start();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationSettings();
        } else {
            askNotificationPermission();
        }
    }

    private void checkNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null && !notificationManager.areNotificationsEnabled()) {
                askNotificationPermission();
            } else {
                handleNotificationPermissionGranted();
            }
        }
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                handleNotificationPermissionGranted();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
    }

    private void handleNotificationPermissionGranted() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleNotificationPermissionGranted();
            } else {
                Toast.makeText(this, "Ilmoitukset ovat poissa käytöstä, laita asetuksista ilmoitukset päälle.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String performLogin(String serverUrl, String username, String password) {
        try {
            URL url = new URL(serverUrl + "/login.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String postData = "action=login&username=" + username + "&password=" + password;

            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes());
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString().trim();

        } catch (IOException e) {
            Log.e("MainActivity", "IOException occurred", e);
            return "Error:" + e.getMessage();
        }
    }

    private void handleLoginResult(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            String status = jsonObject.getString("status");

            if ("success".equals(status)) {
                if (jsonObject.has("usertype")) {
                    String userType = jsonObject.getString("usertype");
                    if ("admin".equals(userType)) {
                        Toast.makeText(LoginActivity.this, "Kirjauduit sisään ADMIN käyttäjällä!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "Kirjauduttu sisään!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Väärä käyttäjänimi/salasana", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Väärä käyttäjänimi/salasana", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e("MainActivity", "JSONException occurred", e);
            Toast.makeText(LoginActivity.this, "Ei saatu yhteyttä palvelimeen, tarkista internet yhteytesi!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendOrderDetailsByEmail(String storeName, String yTunnus, String osoite, String posti, String city, String email, String pnumero, String quantityType) {
        final String username = getString(R.string.email);
        final String password = getString(R.string.emailpass);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.host", "mail3.webnode.com");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("xxx, xxx2"));
            message.setSubject("Käyttäjätunnuksen lisäys " + storeName);

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Kaupan nimi: ").append(storeName).append("\n");
            messageBuilder.append("Puhelinumero: ").append(pnumero).append("\n");
            messageBuilder.append("Sähköpostiosoite: ").append(email).append("\n");
            messageBuilder.append("Y-Tunnus: ").append(yTunnus).append("\n");
            messageBuilder.append("Osoite: ").append(osoite).append("\n");
            messageBuilder.append("Postinumero: ").append(posti).append("\n");
            messageBuilder.append("Kaupunki: ").append(city).append("\n");
            messageBuilder.append("Laskutustyypi: ").append(quantityType).append("\n");


            message.setText(messageBuilder.toString());

            new Thread(() -> {
                try {
                    Transport.send(message);
                } catch (MessagingException e) {
                    Log.e("MainActivity", "MessagingException occurred", e);
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Virhe lähetettäessä sähköpostia: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();

        } catch (MessagingException e) {
            Log.e("MainActivity", "MessagingException occurred", e);
            Toast.makeText(this, "Virhe lähetettäessä sähköpostia: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
