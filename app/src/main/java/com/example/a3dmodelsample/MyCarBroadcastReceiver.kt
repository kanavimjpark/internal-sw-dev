package com.example.a3dmodelsample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyCarBroadcastReceiver : BroadcastReceiver() {

    interface CommandListener {
        fun onCommandReceived(action: String?, position: String?)
    }

    companion object {
        private const val TAG = "MyCarReceiver"

        const val ACTION_OPEN = "com.kanavi.heyari.ACTION_OPEN"
        const val ACTION_CLOSE = "com.kanavi.heyari.ACTION_CLOSE"
        const val ACTION_ON = "com.kanavi.heyari.ACTION_ON"
        const val ACTION_OFF = "com.kanavi.heyari.ACTION_OFF"
        const val ACTION_DOWN = "com.kanavi.heyari.ACTION_DOWN"
        const val ACTION_UP = "com.kanavi.heyari.ACTION_UP"
        const val ACTION_ROTATE = "com.kanavi.heyari.ACTION_ROTATE"
        const val ACTION_ZOOMIN = "com.kanavi.heyari.ACTION_ZOOMIN"
        const val ACTION_ZOOMOUT = "com.kanavi.heyari.ACTION_ZOOMOUT"
        const val ACTION_UNKNOWN = "com.kanavi.heyari.ACTION_UNKNOWN"

        const val EXTRA_POSITION = "position"

        @Volatile
        var listener: CommandListener? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val position = intent.getStringExtra(EXTRA_POSITION)

        Log.d(TAG, "received action=$action, position=$position")

        when (action) {
            ACTION_OPEN,
            ACTION_CLOSE,
            ACTION_ON,
            ACTION_OFF,
            ACTION_DOWN,
            ACTION_UP,
            ACTION_ROTATE,
            ACTION_ZOOMIN,
            ACTION_ZOOMOUT,
            ACTION_UNKNOWN -> {
                listener?.onCommandReceived(action, position)
            }

            else -> {
                Log.w(TAG, "Unhandled action=$action, position=$position")
            }
        }
    }
}