package com.example.lenovossd.gurdianwatch.Common;

import android.location.Location;

import com.example.lenovossd.gurdianwatch.Model.User;
import com.example.lenovossd.gurdianwatch.Remote.IGoogleAPI;
import com.example.lenovossd.gurdianwatch.Remote.RetrofitClient;

public class Common {
    public static final String Child_information_tb1 = "ChildInformation";
    public static final String Child_user_tb1 = "User";
    public static final String user_field ="usr";
    public static final int PICK_IMAGE_REQUEST =9999 ;
    public static final String pwd_field ="pwd";
    public static final String Gallery ="Gallery";
    public static final String child_Loacion ="Location";
    public static Location mLastLocation=null;
    public static final String baseURL = "https://maps.googleapis.com";
    public static User currentUser = new User(  ) ;


    public static IGoogleAPI getGooogleAPI()
    {
        return RetrofitClient.getClient( baseURL ).create(IGoogleAPI .class );

    }

}
