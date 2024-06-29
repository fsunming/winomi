package com.example.onto_1921;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UserService {
    // 이미지 가져오는 메서드
    @GET("getimage")
    Call<UserImage> getUserImage(@Query("userid") String userId);
}

