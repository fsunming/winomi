package com.example.onto_1921;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadService {
    // 사용자 아이디와 이미지 함께 업로드 메서드 정의
    @Multipart
    @POST("upload") // 업로드 URL에 따라 수정 필요
    Call<Void> uploadImage(@Part MultipartBody.Part image, @Part("userId") RequestBody userId);
}
