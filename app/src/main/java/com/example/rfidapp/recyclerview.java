package com.example.rfidapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.rfidapp.Retrofit.RetrofitInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class recyclerview extends AppCompatActivity {

    List<User> userList;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);

        userList = new ArrayList<>();
        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new RecyclerAdapter(getApplicationContext(),userList);
        recyclerView.setAdapter(recyclerAdapter);

        RetrofitInterface apiService = ApiClient.getClient().create(RetrofitInterface.class);
        Call<List<User>> call = apiService.getUsers();

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                userList = response.body();
                Log.d("TAG","Response = "+userList);
                recyclerAdapter.setMovieList(userList);
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.d("TAG","Response = "+t.toString());
            }
        });
    }
}
