package edu.upc.dsa.eetaclife.api;

import edu.upc.dsa.eetaclife.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface EetacApiService {
    @POST("api/estudiants/registre")
    Call<User> registerUser(@Body User user);

    @POST("api/estudiants/login")
    Call<User> loginUser(@Body User user);
}