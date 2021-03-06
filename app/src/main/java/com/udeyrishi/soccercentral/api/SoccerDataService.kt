package com.udeyrishi.soccercentral.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.udeyrishi.soccercentral.BuildConfig
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.money.Money
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.net.URL

/**
 * Created by Udey Rishi (udeyrishi) on 2017-09-08.
 * Copyright © 2017 Udey Rishi. All rights reserved.
 */
interface SoccerDataService {
    @GET("competitions")
    fun getSeasons(@Query("season") year: Year? = null): Observable<List<Season>>

    @GET("competitions/{id}/teams")
    fun getTeams(@Path("id") competitionId: Int): Observable<TeamList>

    companion object {
        const private val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"

        private fun createGson() = GsonBuilder()
                .registerTypeAdapter(Year::class.java, JsonDeserializer<Year> { json, _, _ -> Year(json.asString) })
                .registerTypeAdapter(Money::class.java, JsonDeserializer<Money> { json, _, _ -> Money.parse(json.asString) })
                .registerTypeAdapter(URL::class.java, JsonDeserializer<URL> { json, _, _ -> URL(json.asString) })
                .registerTypeAdapter(TeamList::class.java, JsonDeserializer<TeamList> { json, _, _ ->
                    val gson = Gson()
                    val jsonObject = json.asJsonObject
                    val teamList = TeamList(jsonObject["count"].asInt)
                    jsonObject["teams"].asJsonArray.forEach({ teamList.add(gson.fromJson(it, Team::class.java)) })
                    teamList
                })
                .setDateFormat(DATE_FORMAT)
                .create()

        private fun createRequestHeaderInjector(authToken: String) = Interceptor {
            it.proceed(it.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Auth-Token", authToken)
                    .addHeader("X-Response-Control", "minified")
                    .build()
            )
        }

        private fun createHttpClient(authToken: String) = OkHttpClient.Builder().apply {
            addInterceptor(createRequestHeaderInjector(authToken))
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            }
        }.build()

        private fun <T> Observable<T>.smartFetch(): Observable<T> = this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

        private fun smartFetchWrap(innerService: SoccerDataService) = object: SoccerDataService {
            override fun getSeasons(year: Year?) = innerService.getSeasons(year).smartFetch()
            override fun getTeams(competitionId: Int) = innerService.getTeams(competitionId).smartFetch()
        }

        fun create(apiUrl: String, authToken: String): SoccerDataService {
            return smartFetchWrap(Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(createGson()))
                    .baseUrl(apiUrl)
                    .client(createHttpClient(authToken))
                    .build()
                    .create(SoccerDataService::class.java)
            )
        }
    }
}

