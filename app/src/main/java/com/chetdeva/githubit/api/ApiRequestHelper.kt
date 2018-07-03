package com.chetdeva.githubit.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/**
 * @author chetansachdeva
 */

object ApiRequestHelper {

    inline fun <T> syncRequest(request: Call<T>,
                               onSuccess: (T?) -> Unit,
                               onError: (String) -> Unit) {
        try {
            val response = request.execute()
            onSuccess(response.body())
        } catch (exception: IOException) {
            onError(exception.message ?: "unknown error")
        }
    }

    inline fun <T> asyncRequest(request: Call<T>,
                                crossinline onSuccess: (T?) -> Unit,
                                crossinline onError: (String) -> Unit) {

        request.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                onError(t.message ?: "unknown err")
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    onSuccess(response.body())
                } else {
                    onError("error code: ${response.code()}")
                }
            }
        })
    }
}