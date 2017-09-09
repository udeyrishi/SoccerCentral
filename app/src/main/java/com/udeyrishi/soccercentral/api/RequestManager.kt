package com.udeyrishi.soccercentral.api

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * Created by Udey Rishi (udeyrishi) on 2017-09-09.
 * Copyright © 2017 Udey Rishi. All rights reserved.
 */
class RequestManager: LifecycleObserver {
    private val enRouteRequests = mutableListOf<Disposable>()

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun disposeAll() {
        enRouteRequests.forEach({ it.dispose() })
        enRouteRequests.removeAll({ true })
    }

    fun onStart(request: Disposable) {
        enRouteRequests.add(request)
    }

    fun onComplete(request: Disposable) {
        enRouteRequests.remove(request)
    }
}

fun <T> Observable<T>.managedSubscribe(requestManager: RequestManager, resolve: (T) -> Unit, reject: (Throwable) -> Unit): Disposable {
    val lock = Any()

    // Need to sync this in case this function call and the consume/onError callbacks happen
    // on different threads. If so, there's a potential race condition where the disposable maybe
    // first removed and then added to the list. The parent acquiring the lock guarantees that
    // the disposable will be added first.
    synchronized(lock) {
        var disposable: Disposable? = null
        disposable = this.subscribe(
                {
                    synchronized(lock) {
                        requestManager.onComplete(disposable!!)
                        resolve(it)
                    }
                },
                {
                    synchronized(lock) {
                        requestManager.onComplete(disposable!!)
                        reject(it)
                    }
                }
        )
        requestManager.onStart(disposable)
        return disposable
    }
}