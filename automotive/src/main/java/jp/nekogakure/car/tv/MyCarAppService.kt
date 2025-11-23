package jp.nekogakure.car.tv

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class MyCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        try {
            val builderClass = Class.forName("androidx.car.app.validation.HostValidator\$Builder")
            val ctor = try {
                builderClass.getDeclaredConstructor(android.content.pm.PackageManager::class.java)
            } catch (_: NoSuchMethodException) {
                null
            }
            if (ctor != null) {
                ctor.isAccessible = true
                val builder = ctor.newInstance(packageManager)
                val buildMethod = builderClass.getMethod("build")
                @Suppress("UNCHECKED_CAST")
                return buildMethod.invoke(builder) as HostValidator
            }

            val hvCtor = HostValidator::class.java.getDeclaredConstructor(
                android.content.pm.PackageManager::class.java,
                Map::class.java,
                java.lang.Boolean.TYPE
            )
            hvCtor.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            return hvCtor.newInstance(packageManager, emptyMap<String, List<String>>(), true) as HostValidator
        } catch (e: Exception) {
            throw RuntimeException("Failed to create HostValidator via reflection", e)
        }
    }

    override fun onCreateSession(): Session {
        return object : Session() {
            override fun onCreateScreen(intent: Intent): Screen {
                return MainScreen(carContext)
            }
        }
    }
}
