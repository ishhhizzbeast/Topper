package com.example.topper.session

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.topper.util.Constants.ACTION_SERVICE_CANCEL
import com.example.topper.util.Constants.ACTION_SERVICE_START
import com.example.topper.util.Constants.ACTION_SERVICE_STOP
import com.example.topper.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.topper.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.topper.util.Constants.NOTIFICATION_ID
import com.example.topper.util.pad
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class SessionTimerService (

) : Service(){
    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder : NotificationCompat.Builder
    override fun onBind(p0: Intent?) = binder
    private val binder = SessionTimerBinder()
    private lateinit var timer: Timer
    var duration: Duration = Duration.ZERO
        private set
    var seconds = mutableStateOf("00")
        private set
    var minutes = mutableStateOf("00")
        private set
    var hours = mutableStateOf("00")
        private set
    var currentTimerState = mutableStateOf(TimerState.IDLE)
        private set
    var subjectID = MutableStateFlow<Int?>(null)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action.let {action->
            when(action){
                ACTION_SERVICE_START -> {
                    startForegroundServices()
                    startTimer { h, m, s ->
                        updateNotification(h,m,s)
                    }
                }
                ACTION_SERVICE_STOP -> {
                    stopTimer()
                }
                ACTION_SERVICE_CANCEL -> {
                    stopTimer()
                    cancelTimer()
                    stopForegroundServices()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundServices(){
        createNotificationChannel()
        startForeground(NOTIFICATION_ID,notificationBuilder.build())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
    private fun startTimer(
        onTick: (h: String, m: String, s: String) -> Unit
    ) {
        currentTimerState.value = TimerState.STARTED
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(hours.value, minutes.value, seconds.value)
        }
    }
    private fun updateNotification(
        hours:String,Minutes:String,seconds:String
    ){
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder
                .setContentText("$hours:$Minutes:$seconds")
                .build()
        )
    }
    private fun updateTimeUnits() {
        duration.toComponents { hours, minutes, seconds, _ ->
            this@SessionTimerService.hours.value = hours.toInt().pad()
            this@SessionTimerService.minutes.value = minutes.pad()
            this@SessionTimerService.seconds.value = seconds.pad()
        }
    }

    private fun stopTimer(){
        if (this::timer.isInitialized){
            timer.cancel()
        }
        currentTimerState.value = TimerState.STOPPED
    }

    private fun cancelTimer(){
        duration = Duration.ZERO
        updateTimeUnits()
        currentTimerState.value = TimerState.IDLE
    }
    private fun stopForegroundServices(){
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    inner class SessionTimerBinder: Binder(){
        fun getService() : SessionTimerService = this@SessionTimerService
    }
}
enum class TimerState {
    IDLE,
    STARTED,
    STOPPED
}