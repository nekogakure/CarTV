package jp.nekogakure.car.tv

import android.content.Intent
import androidx.car.app.Session
import androidx.car.app.Screen

class MySession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return MainScreen(carContext)
    }
}