package com.udeyrishi.soccercentral.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

/**
 * Created by Udey Rishi (udeyrishi) on 2017-09-08.
 * Copyright © 2017 Udey Rishi. All rights reserved.
 */
interface SoccerDataService {
    @GET("competitions")
    fun getSeasons(@Query("season") year: Year? = null): Observable<List<Season>>

    companion object {
        private val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"

        fun create(apiUrl: String, authToken: String): SoccerDataService =
            Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory
                                    .create(
                                            GsonBuilder()
                                                    .registerTypeAdapter(Year::class.java, JsonSerializer<Year> { year, _, _ -> JsonPrimitive(year.toString()) })
                                                    .registerTypeAdapter(Year::class.java, JsonDeserializer<Year> { json, _, _ -> Year(json.asString) })
                                                    .setDateFormat(DATE_FORMAT)
                                                    .create()
                                    )
                    )
                    .baseUrl(apiUrl)
                    .client(OkHttpClient.Builder()
                            .addInterceptor {
                                it.proceed(it.request().newBuilder()
                                        .addHeader("Content-Type", "application/json")
                                        .addHeader("X-Auth-Token", authToken)
                                        .addHeader("X-Response-Control", "minified")
                                        .build()
                                )
                            }
                            .build())
                    .build()
                    .create(SoccerDataService::class.java)
    }
}

fun <T> Observable<T>.smartFetch(): Observable<T> = this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

class Year(year: String) {
    private val year: String = (Regex("^\\d{4}$").matchEntire(year) ?: throw IllegalArgumentException("Illegal year string")).groupValues[0]

    override fun toString() = year

    override fun equals(other: Any?) = other is Year && this.year == other.year

    override fun hashCode() = year.hashCode() * 7
}

data class Season(val id: Int,
                  val caption: String,
                  val league: String,
                  val year: Year,
                  val currentMatchday: Int,
                  val numberOfMatchdays: Int,
                  val numberOfTeams: Int,
                  val lastUpdated: Date)

