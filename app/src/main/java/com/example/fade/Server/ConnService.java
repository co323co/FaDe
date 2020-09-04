package com.example.fade.Server;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ConnService {

    public static final String URL = "http://220.123.36.108:1213";

//    @GET("/{userId}")
//    Call<RegiData> getFirst(@Path("userId") String id);

    @GET("/db/download/{uid}")
    Call<ReturnData> getDB(@Path("uid") String uid);

    @PUT("/db/upload/{uid}")
    Call<ResponseBody> putDB(@Path("uid") String uid, @Body JSONObject dbFiles);

    @FormUrlEncoded
    @POST("/db/upload/{uid}")
    Call<ResponseBody> postDB(@Path("uid") String uid, @FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/reg/person")
    Call<ReturnData> postRegisterPerson(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/reg/group")
    Call<ReturnData> postRegisterGroup(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/det")
    Call<ReturnData> postDetectionPicture(@FieldMap HashMap<String, Object> param);

}




