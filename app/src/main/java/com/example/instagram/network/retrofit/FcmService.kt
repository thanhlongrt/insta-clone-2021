package com.example.instagram.network.retrofit

import com.example.instagram.Constants
import com.example.instagram.network.entity.FcmMessage
import com.example.instagram.network.entity.FcmResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by Thanh Long Nguyen on 5/7/2021
 */
interface FcmService {

    @Headers(
        "Authorization:key=${Constants.SERVER_KEY}",
        "Content-Type:application/json"
    )
    @POST("fcm/send")
    suspend fun sendPushNotification(@Body body: FcmMessage): Response<FcmResponse>

}