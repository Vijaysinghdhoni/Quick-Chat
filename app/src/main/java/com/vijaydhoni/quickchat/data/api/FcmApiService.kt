package com.vijaydhoni.quickchat.data.api

import com.vijaydhoni.quickchat.data.models.SendMessage
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApiService {

    @POST("/send")
    suspend fun sendMessage(
        @Body body: SendMessage
    )

}