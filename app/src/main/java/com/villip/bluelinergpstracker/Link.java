package com.villip.bluelinergpstracker;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by villip on 25.07.2017.
 */

public interface Link {

    @POST("index.php")
    Call<ResponseBody> addPost(@Body GPSPost gpsPost);


    /*@FormUrlEncoded
    @POST("http://45.55.132.244/blue_lainer/bl_api_v1/traking/index.php")
    Call<ResponseBody> addPost(@Field("imei") String imei,
                               @Field("longitude") double longitude,
                               @Field("latitude") double latitude,
                               @Field("time") String time,
                               @Field("speed") String speed);*/
}
