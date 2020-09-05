package com.example.fade;

import com.example.fade.Server.RegiData;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FlaskService {

    public static final String URL = "http://192.168.25.41:5000";


    @GET("/{userId}")
    Call<RegiData> getFirst(@Path("userId") String id);


    @FormUrlEncoded
    @POST("/reg/person")
    Call<RegiData> postRegisterPerson(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/reg/group")
    Call<RegiData> postRegisterGroup(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/det")
    Call<RegiData> postDetectionPicture(@FieldMap HashMap<String, Object> param);

}
