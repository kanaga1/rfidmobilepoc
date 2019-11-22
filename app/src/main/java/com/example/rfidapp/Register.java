package com.example.rfidapp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rfidapp.Retrofit.RetrofitInterface;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Register extends AppCompatActivity {
    private Retrofit retrofit;
    TextView login;
    private  String name="",email="",pass="";
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://192.168.0.100:3000";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100,TimeUnit.SECONDS).build();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Button signupBtn = findViewById(R.id.signup);
        final EditText nameEdit = findViewById(R.id.nameEdit);
        final EditText emailEdit = findViewById(R.id.emailEdit);
        final EditText passwordEdit = findViewById(R.id.passwordEdit);
        final TextView signintv = findViewById(R.id.signintv);
        retrofitInterface = retrofit.create(RetrofitInterface.class);
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String, String> map = new HashMap<>();

                map.put("name", nameEdit.getText().toString());
                name = nameEdit.getText().toString();
                map.put("email", emailEdit.getText().toString());
                email = emailEdit.getText().toString();
                map.put("password", passwordEdit.getText().toString());
                pass = passwordEdit.getText().toString();
                if (name.equals("")||email.equals("")||pass.equals(""))
                {
                    Toast.makeText(Register.this,
                            "Please enter all fields..", Toast.LENGTH_SHORT).show();

                }
                else {
                    Call<Void> call = retrofitInterface.executeSignup(map);

                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {

                            if (response.code() == 200) {
                                Toast.makeText(Register.this,
                                        "Signed up successfully", Toast.LENGTH_SHORT).show();

                            } else if (response.code() == 400) {
                                Toast.makeText(Register.this,
                                        "Already registered", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(Register.this, t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });

        signintv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }
}
