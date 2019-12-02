package com.example.chickenrun;


import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

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


}


