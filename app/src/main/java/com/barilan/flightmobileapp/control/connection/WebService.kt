package com.barilan.flightmobileapp.control.connection

import com.barilan.flightmobileapp.control.data.Command
import com.barilan.flightmobileapp.control.data.Result
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface WebService {
    @GET("/screenshot")
    fun getImg(): Deferred<ResponseBody>
    @POST("/api/Command")
    fun setCommand(@Body command: Command) : Deferred<Result>
}