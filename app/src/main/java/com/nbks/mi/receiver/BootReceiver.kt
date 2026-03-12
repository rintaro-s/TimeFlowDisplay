package com.nbks.mi.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nbks.mi.worker.ScreenDimWorker
import dagger.hilt.android.AndroidEntryPoint

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ScreenDimWorker.schedule(context)
        }
    }
}
