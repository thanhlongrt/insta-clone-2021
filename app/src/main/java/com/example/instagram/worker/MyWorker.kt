package com.example.instagram.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Created by Thanh Long Nguyen on 5/11/2021
 */
class MyWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object{
        private const val TAG = "MyWorker"
    }

    override fun doWork(): Result {
        Log.e(TAG, "doWork: ", )
        return Result.success()
    }
}