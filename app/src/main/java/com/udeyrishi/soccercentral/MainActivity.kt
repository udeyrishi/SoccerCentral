package com.udeyrishi.soccercentral

import android.arch.lifecycle.LifecycleActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.udeyrishi.soccercentral.api.RequestManager
import com.udeyrishi.soccercentral.api.managedSubscribe

class MainActivity : LifecycleActivity() {
    companion object {
        val LOG_TAG: String = MainActivity::class.java.name
    }

    val textView: TextView by lazy { findViewById<TextView>(R.id.text_view) }
    val requestManager by lazy { RequestManager().apply { lifecycle.addObserver(this) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        App.instance.soccerDataService.getSeasons().managedSubscribe(
                requestManager,
                { seasons -> textView.text = seasons[0].toString() },
                { Log.e(LOG_TAG, it.message) }
        )
    }
}
