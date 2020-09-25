package com.example.fade.Server;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ConnService {


    //혜림
//    public static final String URL = "http://192.168.219.103:5000";
    //민정
    public static final String URL = "http://192.168.35.155:5000";
    //다빈
    //public static final String URL = "http://192.168.25.41:3157";


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
    @POST("/gallery/upload/{uid}")
    Call<ResponseBody> postGalleryImg(@Path("uid") String uid, @FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/reg/person")
    Call<ReturnData> postRegisterPerson(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/reg/group")
    Call<ResponseBody> postRegisterGroup(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/det/{uid}")
    Call<ResponseBody> postDetectionPicture(@Path("uid") String uid, @FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/edit/group")
    Call<ResponseBody> postEditGroup(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/delete/group")
    Call<ResponseBody> DeleteGroup(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/delete/person")
    Call<ResponseBody> DeletePerson(@FieldMap HashMap<String, Object> param);

}




