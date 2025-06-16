package com.angeldevtech.gol.utils

import android.app.PendingIntent
import android.app.RemoteAction
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import com.angeldevtech.gol.R

const val ACTION_PLAY = "com.angeldevtech.gol.PLAY"
const val ACTION_PAUSE = "com.angeldevtech.gol.PAUSE"

fun createPipActions(
    context: Context,
    isPlaying: Boolean
): List<RemoteAction> {
    val actions = mutableListOf<RemoteAction>()

    if (isPlaying) {
        val pauseIntent = Intent(ACTION_PAUSE).setPackage(context.packageName)
        val pausePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            pauseIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val pauseIcon = Icon.createWithResource(context, R.drawable.ic_pause)
        val pauseTitle = "Pause"

        actions.add(RemoteAction(pauseIcon, pauseTitle, pauseTitle, pausePendingIntent))
    } else {
        val playIntent = Intent(ACTION_PLAY).setPackage(context.packageName)
        val playPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            playIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val playIcon = Icon.createWithResource(context, R.drawable.ic_play)
        val playTitle = "Play"

        actions.add(RemoteAction(playIcon, playTitle, playTitle, playPendingIntent))
    }

    return actions
}