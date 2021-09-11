package com.example.fade.Server;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ConnService {


    //혜림
    public static final String URL = "http://192.168.219.106:5000";

    //public static final String URL = "http://192.168.0.5:5000";
    //민정
//    public static final String URL = "http://192.168.0.10:5000";
    //다빈
    //public static final String URL = "http://192.168.25.41:3157";

    @PUT("/users/{userEmail}/new")
    Call<ResponseBody> putRegisterUser(@Path("userEmail") String userEmail);

    @GET("/users/{userEmail}/last-update-date")
    Call<ResponseBody> getLastUpdate(@Path("userEmail") String userEmail);

    @GET("/users/{userEmail}/all-groups")
    Call<List<GroupData>> getAllGroups(@Path("userEmail") String userEmail);

    @GET("/users/{userEmail}/all-people")
    Call<List<PersonData>> getAllPersons(@Path("userEmail") String userEmail);

    @GET("/groups/{gid}/all-pids")
    Call<List<Integer>> getPidListByGid(@Path("gid") int gid);

    @GET("/groups/{gid}/all-people")
    Call<List<PersonData>> getPersonsByGid(@Path("gid") int gid);

    @GET("/people/{pid}/all-groups")
    Call<List<GroupData>> getGroupsByPid(@Path("pid") int pid);

    @FormUrlEncoded
    @POST("/users/upload/{uid}")
    Call<ResponseBody> postDB(@Path("uid") String uid, @FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("/gallery/{uid}")
    Call<ResponseBody> postGalleryImg(@Path("uid") String uid, @FieldMap HashMap<String, Object> param);

    //인물 등록
    @FormUrlEncoded
    @POST("/people/new")
    Call<ResponseBody> postRegisterPerson(@FieldMap HashMap<String, Object> param);

    //그룹 등록
    @FormUrlEncoded
    @POST("/groups/new")
    Call<ResponseBody> postRegisterGroup(@FieldMap HashMap<String, Object> param);

    //사진 분류 및 이동
    @FormUrlEncoded
    @POST("/det/{userEmail}")
    Call<List<String>> postDetectionPicture(@Path("userEmail") String userEmail, @FieldMap HashMap<String, Object> param);

    //그룹 수정
    @FormUrlEncoded
    @POST("/groups/edit")
    Call<ResponseBody> postEditGroup(@FieldMap HashMap<String, Object> param);

    //그룹 삭제
    @FormUrlEncoded
    @POST("/groups/delete")
    Call<ResponseBody> postDeleteGroup(@FieldMap HashMap<String, Object> param);

    //인물 삭제
    @FormUrlEncoded
    @POST("/people/delete")
    Call<ResponseBody> postDeletePerson(@FieldMap HashMap<String, Object> param);

}




