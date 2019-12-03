package com.example.chickenrun;


import com.example.chickenrun.Lobby.item_lobby_list;
import com.example.chickenrun.gameRoom.item_participant_user;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface
{
    // ex) 사진 목록 불러오기
//    @FormUrlEncoded
//    @POST("getImage.php")
//    Call<List<Item_Photo>> getPhoto(@Field("") String eeeeeeeing);

    //회원 로그인
    @FormUrlEncoded
    @POST("chicken/memberchek.php")
    Call<Resultm> logincheck(@FieldMap HashMap<String, Object> param);


    //회원의 치킨 갯수 가져오기
    @GET("chicken/userchickencnt.php")
    Call<Resultm> memvbercnt(@Query("id") String id);

    // 로비에서 방 목록 불러오기
    @FormUrlEncoded
    @POST("chicken/getRoomList.php")
    Call<List<item_lobby_list>> getRoomList(@Field("getId") String id);


    // 방 참가자 목록 불러오기
    @FormUrlEncoded
    @POST("chicken/.php")
    Call<List<item_participant_user>> getParacticipant(@Field("getId") String id);



}


