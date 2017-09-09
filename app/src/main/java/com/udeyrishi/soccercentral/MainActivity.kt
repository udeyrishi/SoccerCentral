package com.udeyrishi.soccercentral

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import io.reactivex.disposables.Disposable

class MainActivity : AppCompatActivity() {
    companion object {
        val LOG_TAG: String = MainActivity::class.java.name
    }

    lateinit var textView: TextView
    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById<TextView>(R.id.text_view)
    }

    override fun onResume() {
        super.onResume()
        disposable = App.instance.soccerDataService.getSeasons().subscribe(
                { seasons -> textView.text = seasons[0].toString() },
                { Log.e(LOG_TAG, it.message) }
        )
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }
}
