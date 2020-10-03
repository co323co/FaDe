package com.example.fade.Server;

import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ConnService {


    //혜림
    public static final String URL = "http://192.168.0.5:5000";
    //민정
    //public static final String URL = "http://192.168.0.10:5000";
    //다빈
    //public static final String URL = "http://192.168.25.41:3157";

    @PUT("/Login/{userEmail}")
    Call<ResponseBody> putRegisterUser(@Path("userEmail") String userEmail);

    @GET("/db/GetAllGroups/{userEmail}")
    Call<List<GroupData>> getAllGroups(@Path("userEmail") String userEmail);

    @GET("/db/GetAllPersons/{userEmail}")
    Call<List<PersonData>> getAllPersons(@Path("userEmail") String userEmail);

    @GET("/db/GetPidListByGid/{gid}")
    Call<List<Integer>> getPidListByGid(@Path("gid") int gid);

    @GET("/db/GetPersonsByGid/{gid}")
    Call<List<PersonData>> getPersonsByGid(@Path("gid") int gid);

    @GET("/db/GetGroupsByPid/{pid}")
    Call<List<GroupData>> getGroupsByPid(@Path("pid") int pid);

    @FormUrlEncoded
    @POST("/db/upload/{uid}")
    Call<ResponseBody> postDB(@Path("uid") String uid, @FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/gallery/upload/{uid}")
    Call<ResponseBody> postGalleryImg(@Path("uid") String uid, @FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/reg/person")
    Call<ResponseBody> postRegisterPerson(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/reg/group")
    Call<ResponseBody> postRegisterGroup(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/det/{userEmail}")
    Call<List<String>> postDetectionPicture(@Path("userEmail") String userEmail, @FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/edit/group")
    Call<ResponseBody> postEditGroup(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/delete/group")
    Call<ResponseBody> postDeleteGroup(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/delete/person")
    Call<ResponseBody> postDeletePerson(@FieldMap HashMap<String, Object> param);

}




