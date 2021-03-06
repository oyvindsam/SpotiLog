package com.developments.samu.spotlog.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.developments.samu.spotlog.R
import com.developments.samu.spotlog.SpotLogApplication
import com.developments.samu.spotlog.data.Song
import com.developments.samu.spotlog.data.SongRepository
import com.developments.samu.spotlog.data.sameNotNewPosition
import com.developments.samu.spotlog.data.toPositionPrettyString
import com.developments.samu.spotlog.log.LogActivity
import com.developments.samu.spotlog.preference.PrefsFragment
import com.developments.samu.spotlog.utilities.Spotify
import com.developments.samu.spotlog.utilities.getIntOrDefault
import javax.inject.Inject


/**
 * Service for listening to intents sent by Spotify, and saving new songs to repository.
 * The service can either be foreground or background. When the user has the app open the background
 * service should run. If the user toggles 'Foreground Service' ON in Settings, the
 * app should start the foreground service when the user EXITS the app. A foreground service on API >= 26
 * needs to have an active notification to live. A background service will be killed in ~1 min if the user exits
 * the app.
 */
class LoggerService : Service() {

    private val LOG_TAG: String = LoggerService::class.java.simpleName

    private val spotifyReceiver = Spotify.spotifyReceiver(::log)

    private val notifPendingIntent by lazy { PendingIntent.getActivity(
            this,
            0,
            Intent(this, LogActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP},
            0) }

    private val notifPendingStop by lazy {
        PendingIntent.getService(
                this,
                0,
                Intent(this, LoggerService::class.java).apply {
                    action = LoggerService.ACTION_STOP
                },
                0)
    }

    private val notifActionStop by lazy {
        NotificationCompat.Action.Builder(
                R.drawable.ic_clear,
                getString(R.string.notif_action_title),
                notifPendingStop)
                .build()
    }

    private val notificationBuilder by lazy {
        NotificationCompat.Builder(this, LoggerService.DEFAULT_CHANNEL)
                .setSmallIcon(R.drawable.ic_tile_log_track)
                .setContentTitle(getString(R.string.notif_content_title))
                .setContentText("No song detected yet")
                .setContentIntent(notifPendingIntent)
                .addAction(notifActionStop)
    }

    @Inject
    lateinit var repository: SongRepository

    @Inject
    lateinit var prefs: SharedPreferences

    private var lastSong = Song(
            "",
            "",
            "",
            "",
            0,
            0,
            0
    )

    init {
        SpotLogApplication.getAppComponent().injectLoggerService(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            LoggerService.ACTION_STOP -> stopSelf()  // onDestroy called.. variable over is not necessary?
            LoggerService.ACTION_START_FOREGROUND -> startService()
        }
        return START_STICKY
    }

    // start backgroundReceiver. Start foreground service if specified
    private fun startService() {
        if (running) return
        running = true
        registerReceiver(spotifyReceiver, Spotify.INTENT_FILTER)  // start backgroundReceiver for picking up Spotify intents
        startForegroundNotif()
    }

    // start foreground service and check for latest logged song to put in notification content text.
    private fun startForegroundNotif() {
        startForeground(LoggerService.NOTIFICATION_ID, notificationBuilder.build())
        repository.getLastLoggedSong(::log)
    }

    private fun log(song: Song) {
        // first init after service created
        if (lastSong.trackId == "") lastSong = song
        else if (song.sameNotNewPosition(lastSong)) return

        val logSize = prefs.getIntOrDefault(PrefsFragment.PREF_LOG_SIZE_KEY)
        repository.logSong(logSize, song)
        notifySongLogged(song)
        lastSong = song
    }

    private fun notifySongLogged(song: Song) {
        notificationBuilder.apply {
            setContentTitle("${song.track} - ${song.artist}")
            setContentText(song.toPositionPrettyString())
        }.also {
            NotificationManagerCompat.from(this).notify(LoggerService.NOTIFICATION_ID, it.build())
        }
    }

    override fun onDestroy() {
        try {
            // Throws if not started. onDestroy is called when stopService is called from outside
            unregisterReceiver(spotifyReceiver)
        } catch (e: Exception) {}
        running = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    companion object {
        const val ACTION_START_FOREGROUND = "START_FOREGROUND"
        const val ACTION_STOP = "STOP_SERVICE"
        private var running = false
        fun isServiceRunning() = running  // used in tileservice etc.

        // channel stuff
        const val DEFAULT_CHANNEL = "SPOTLOG_DEFAULT_CHANNEL"
        const val NOTIFICATION_ID = 3245

        // Needs only to be called once (application startup)
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                        LoggerService.DEFAULT_CHANNEL,
                        context.getString(R.string.notif_channel_name),
                        NotificationManager.IMPORTANCE_LOW).apply {
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    description = context.getString(R.string.notif_channel_description)
                }
                val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                service.createNotificationChannel(channel)
            }
        }
    }

}
