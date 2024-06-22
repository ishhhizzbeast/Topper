package com.example.topper

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import com.example.topper.destinations.DashBoardScreenRouteDestination
import com.example.topper.destinations.SesstionScreenRouteDestination
import com.example.topper.domain.model.Session
import com.example.topper.domain.model.Subject
import com.example.topper.domain.model.Task
import com.example.topper.presentation.dashboard.DashBoardScreenRoute
import com.example.topper.session.SessionTimerService
import com.example.topper.ui.theme.TopperTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.spec.NavGraphSpec
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isBound by mutableStateOf(false)
    private lateinit var timerService: SessionTimerService

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as SessionTimerService.SessionTimerBinder
            timerService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, SessionTimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (isBound) {
                TopperTheme {
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        dependenciesContainerBuilder = {
                            dependency(SesstionScreenRouteDestination){
                                timerService
                            }
                        }
                    )
                }
            }
            requestpermission()
        }
    }
        private fun requestpermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }
    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }
}
val subject = listOf(
    Subject("english", 12f, Subject.subjectcardcolors[0].map {
                                                             it.toArgb()
    },0),
    Subject("C++", 2f, Subject.subjectcardcolors[1].map {
        it.toArgb()
    },2),
    Subject("OOP", 12.3f, Subject.subjectcardcolors[2].map {
        it.toArgb()
    },1),
    Subject("kotlin", 1.3f, Subject.subjectcardcolors[3].map {
        it.toArgb()
    },3)
)

val task = listOf(
    Task("Learn flutter", "", 0L, 1, "", true,1,0),
    Task("Learn kotlin", "", 0L, 2, "", false,2,0),
    Task("Learn c++", "", 0L, 3, "", true,3,0),
    Task("Learn trap", "", 0L, 1, "", false,4,0),
    Task("Learn guiter", "", 0L, 2, "", false,5,0)

)