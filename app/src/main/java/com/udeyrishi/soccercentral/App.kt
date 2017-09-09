package com.udeyrishi.soccercentral

import android.app.Application
import com.udeyrishi.soccercentral.api.SoccerDataService

/**
 * Created by Udey Rishi (udeyrishi) on 2017-09-08.
 * Copyright © 2017 Udey Rishi. All rights reserved.
 */
class App: Application() {
    companion object {
        lateinit var instance: App private set
    }

    val soccerDataService by lazy {
        SoccerDataService.create(
                apiUrl = applicationContext.getString(R.string.api_url),
                authToken = applicationContext.getString(R.string.api_auth_token)
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}