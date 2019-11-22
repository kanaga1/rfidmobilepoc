package com.example.rfidapp.Retrofit;

import com.example.rfidapp.LoginResult;
import com.example.rfidapp.User;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;


public interface RetrofitInterface {

    @POST("/login")
    Call<LoginResult> executeLogin(@Body HashMap<String, String> map);

    @GET("/Getrec")
    Call<List<User>> getUsers();

//    @POST("/signup")
//    Call<Void> executeSignup (@Body HashMap<String, String> map);

    @POST("/regis")
    Call<Void> executeSignup (@Body HashMap<String, String> map);




}
