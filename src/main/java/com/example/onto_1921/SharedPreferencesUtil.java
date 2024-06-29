package com.example.onto_1921;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    private static final String USER_PREF = "user_pref";
    private static final String USER_ID_KEY = "userid";

    // SharedPreferences에 사용자 아이디 저장하는 메서드
    public static void saveUserId(Context context, String userid) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID_KEY, userid);
        editor.apply();
    }

    // SharedPreferences에서 사용자 아이디 불러오는 메서드
    public static String getUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_ID_KEY, "");
    }
}
