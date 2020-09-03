package com.example.fade.Server;

import java.util.HashMap;

public interface ConnkService {

    public static final String URL = "http://220.123.36.108:1213";

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
