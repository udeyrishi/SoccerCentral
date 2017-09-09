package com.udeyrishi.soccercentral

import android.arch.lifecycle.LifecycleActivity
import android.os.Bundle
import android.widget.TextView
import com.udeyrishi.soccercentral.api.RequestManager
import com.udeyrishi.soccercentral.api.managedSubscribe
import io.reactivex.Observable

class MainActivity : LifecycleActivity() {
    private val textView: TextView by lazy { findViewById<TextView>(R.id.text_view) }
    private val requestManager by lazy { RequestManager().apply { lifecycle.addObserver(this) } }
    private val soccerDataService get() = App.instance.soccerDataService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        soccerDataService
                .getSeasons()
                .flatMap { seasons -> Observable.just(seasons[0].id) }
                .flatMap { soccerDataService.getTeams(it) }
                .flatMap { teams -> Observable.just(teams[0]) }
                .managedSubscribe(requestManager, { textView.text = it.toString() })
    }
}
