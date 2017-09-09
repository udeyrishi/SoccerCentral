package com.udeyrishi.soccercentral.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Udey Rishi (udeyrishi) on 2017-09-08.
 * Copyright © 2017 Udey Rishi. All rights reserved.
 */
interface SoccerDataService {
    @GET("competitions")
    fun getSeasons(@Query("season") year: Year? = null): Observable<List<Season>>

    companion object {
        const private val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"

        private fun createGson() = GsonBuilder()
                .registerTypeAdapter(Year::class.java, JsonSerializer<Year> { year, _, _ -> JsonPrimitive(year.toString()) })
                .registerTypeAdapter(Year::class.java, JsonDeserializer<Year> { json, _, _ -> Year(json.asString) })
                .setDateFormat(DATE_FORMAT)
                .create()

        private fun createHeaderInterceptor(authToken: String) = Interceptor {
            it.proceed(it.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Auth-Token", authToken)
                    .addHeader("X-Response-Control", "minified")
                    .build()
            )
        }

        private fun <T> Observable<T>.smartFetch(): Observable<T> = this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

        private fun smartFetchWrap(innerService: SoccerDataService) = object: SoccerDataService {
            override fun getSeasons(year: Year?) = innerService.getSeasons(year).smartFetch()
        }

        fun create(apiUrl: String, authToken: String): SoccerDataService {
            return smartFetchWrap(Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(createGson()))
                    .baseUrl(apiUrl)
                    .client(OkHttpClient.Builder().addInterceptor(createHeaderInterceptor(authToken)).build())
                    .build()
                    .create(SoccerDataService::class.java)
            )
        }
    }
}

