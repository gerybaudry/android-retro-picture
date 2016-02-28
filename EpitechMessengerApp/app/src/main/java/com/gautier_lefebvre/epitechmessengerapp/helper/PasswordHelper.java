package com.gautier_lefebvre.epitechmessengerapp.helper;

public class PasswordHelper {
    public static boolean isPasswordValid(String password) {
        String majs = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String mins = "abcdefghijklmnopqrstuvwxyz";
        String figs = "0123456789";

        return password.length() >= 8 &&
                StringHasCharOfString(password, majs) &&
                StringHasCharOfString(password, mins) &&
                StringHasCharOfString(password, figs);
    }

    static boolean StringHasCharOfString(String a, String b) {
        for (int i = 0 ; i < a.length() ; ++i) {
            for (int j = 0 ; j < b.length() ; ++j) {
                if (a.charAt(i) == b.charAt(j)) {
                    return true;
                }
            }
        }
        return false;
    }
}
