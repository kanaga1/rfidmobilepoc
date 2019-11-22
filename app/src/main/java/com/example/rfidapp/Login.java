package com.example.rfidapp;

import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

public class Login extends AppCompatActivity {
    private Retrofit retrofit;
    public String name = "", pass2 = "", email1 = "", pass = "", email = "";
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://192.168.0.103:3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS).build();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final EditText emailEdit = findViewById(R.id.emailEdit);
        final EditText passwordEdit = findViewById(R.id.passwordEdit);
        final TextView signuptv = findViewById(R.id.signuptv);
        retrofitInterface = retrofit.create(RetrofitInterface.class);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String, String> map = new HashMap<>();

                map.put("email", emailEdit.getText().toString());
                map.put("password", passwordEdit.getText().toString());
                email1 = emailEdit.getText().toString();
                System.out.println("ggggggggggggg"+ email1);
                pass2 = passwordEdit.getText().toString();
                System.out.println("ggggggggggggggg"+ pass2);
                Call<LoginResult> call = retrofitInterface.executeLogin(map);

                call.enqueue(new Callback<LoginResult>() {
                    @Override
                    public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {

                        if (response.code() == 200) {

                            LoginResult result = response.body();

                            pass= result.getPassword();
                            email = result.getEmail();

                            System.out.println("fffffffffff"+ email);

                            System.out.println("fffffffffff"+ pass);

                            if(email.equals(email1))
                            {
                                startActivity(new Intent(Login.this,recyclerview.class));
                            }


                        } else if (response.code() == 404) {
                            if(email1.equals("") || pass2.equals("")) {
                                Toast.makeText(Login.this, "Please enter valid credentials", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(Login.this, "Wrong Credentials", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<LoginResult> call, Throwable t) {
                        Toast.makeText(Login.this, t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeee" + t.getMessage() );
                    }
                });
            }
        });


        signuptv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });
    }

}

