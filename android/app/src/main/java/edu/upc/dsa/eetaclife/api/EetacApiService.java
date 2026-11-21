package edu.upc.dsa.eetaclife.api;

import java.util.List;

import edu.upc.dsa.eetaclife.models.AuthResponse;
import edu.upc.dsa.eetaclife.models.CafeteriaItem;
import edu.upc.dsa.eetaclife.models.CafeteriaOrder;
import edu.upc.dsa.eetaclife.models.Credentials;
import edu.upc.dsa.eetaclife.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface EetacApiService {
    @POST("api/estudiants/registre")
    Call<User> registerUser(@Body Credentials credentials);

    @POST("api/estudiants/login")
    Call<AuthResponse> loginUser(@Body Credentials credentials);

    @GET("api/estudiants/me")
    Call<User> getProfile(@Header("Authorization") String bearerToken);

    @DELETE("api/estudiants/me")
    Call<String> deleteAccount(@Header("Authorization") String bearerToken);

    @GET("api/cafeteria/menu")
    Call<List<CafeteriaItem>> getCafeteriaMenu();

    @POST("api/cafeteria/compres")
    Call<User> buyCafeteriaItem(@Header("Authorization") String bearerToken, @Body CafeteriaOrder order);
}