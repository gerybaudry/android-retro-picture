package com.gautier_lefebvre.epitechmessengerapp.entity;

import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.SecretKey;

public class ApplicationData {
    public static CurrentUser currentUser = null;
    public static ArrayList<Contact> contactList = new ArrayList<>();
    public static PublicKey serverKey = null;
    public static SecretKey aesKey = null;
    public static String gcmRegistrationToken = null;

    public static Contact getContactById(int id) {
        for (Contact c : ApplicationData.contactList) {
            if (c.userId == id) {
                return c;
            }
        }
        return null;
    }
}
