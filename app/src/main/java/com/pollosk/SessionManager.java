package com.pollosk;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "ChickenSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_PASSWORD = "appPassword";
    private static final String KEY_QUESTION = "securityQuestion";
    private static final String KEY_ANSWER = "securityAnswer";

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean hasPassword() {
        return !pref.getString(KEY_PASSWORD, "").isEmpty();
    }

    public void setPassword(String password) {
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    public boolean checkPassword(String password) {
        return pref.getString(KEY_PASSWORD, "").equals(password);
    }

    public void setSecurityQA(String question, String answer) {
        editor.putString(KEY_QUESTION, question);
        editor.putString(KEY_ANSWER, answer.toLowerCase().trim());
        editor.apply();
    }

    public String getSecurityQuestion() {
        return pref.getString(KEY_QUESTION, "");
    }

    public boolean checkAnswer(String answer) {
        return pref.getString(KEY_ANSWER, "").equals(answer.toLowerCase().trim());
    }

    public void logout() {
        setLogin(false);
    }
}
