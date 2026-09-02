package com.luanarabelo.treinodaluana.v12

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

object HealthConnectBridge {
    const val PERMISSION_REQUEST_CODE = 1212
    private const val HEALTH_CONNECT_PROVIDER = "com.google.android.apps.healthdata"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val permissions = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class)
    )

    @JvmStatic
    fun requiredPermissions(): Set<String> = permissions

    fun interface StatusCallback {
        fun onResult(available: Boolean, granted: Boolean)
    }

    fun interface WriteCallback {
        fun onResult(success: Boolean, message: String)
    }

    @JvmStatic
    fun checkStatus(context: Context, callback: StatusCallback) {
        val appContext = context.applicationContext
        val status = HealthConnectClient.getSdkStatus(appContext)
        if (status != HealthConnectClient.SDK_AVAILABLE) {
            mainHandler.post { callback.onResult(false, false) }
            return
        }

        scope.launch {
            try {
                val granted = HealthConnectClient.getOrCreate(appContext)
                    .permissionController
                    .getGrantedPermissions()
                    .containsAll(permissions)
                mainHandler.post { callback.onResult(true, granted) }
            } catch (_: Throwable) {
                mainHandler.post { callback.onResult(false, false) }
            }
        }
    }

    @JvmStatic
    fun requestWritePermission(activity: Activity): Boolean {
        if (HealthConnectClient.getSdkStatus(activity) != HealthConnectClient.SDK_AVAILABLE) {
            openProviderStore(activity)
            return false
        }

        return try {
            val contract = PermissionController.createRequestPermissionResultContract()
            val intent = contract.createIntent(activity, permissions)
            activity.startActivityForResult(intent, PERMISSION_REQUEST_CODE)
            true
        } catch (_: Throwable) {
            openSettings(activity)
            false
        }
    }

    @JvmStatic
    fun permissionGrantedFromResult(resultCode: Int, data: Intent?): Boolean {
        return try {
            PermissionController.createRequestPermissionResultContract()
                .parseResult(resultCode, data)
                .containsAll(permissions)
        } catch (_: Throwable) {
            false
        }
    }

    @JvmStatic
    fun writeStrengthWorkout(
        context: Context,
        title: String,
        notes: String,
        startMillis: Long,
        endMillis: Long,
        callback: WriteCallback
    ) {
        val appContext = context.applicationContext
        if (HealthConnectClient.getSdkStatus(appContext) != HealthConnectClient.SDK_AVAILABLE) {
            callback.onResult(false, "Health Connect indisponível")
            return
        }

        scope.launch {
            try {
                val client = HealthConnectClient.getOrCreate(appContext)
                val granted = client.permissionController.getGrantedPermissions()
                if (!granted.containsAll(permissions)) {
                    mainHandler.post { callback.onResult(false, "Permissão de treino não liberada") }
                    return@launch
                }

                val safeStart = Instant.ofEpochMilli(startMillis)
                val safeEnd = Instant.ofEpochMilli(maxOf(endMillis, startMillis + 1_000L))
                val zone = ZoneId.systemDefault()
                val record = ExerciseSessionRecord(
                    metadata = Metadata.manualEntry(),
                    startTime = safeStart,
                    startZoneOffset = zone.rules.getOffset(safeStart),
                    endTime = safeEnd,
                    endZoneOffset = zone.rules.getOffset(safeEnd),
                    exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
                    title = title,
                    notes = notes
                )
                client.insertRecords(listOf(record))
                mainHandler.post { callback.onResult(true, "Treino salvo no Health Connect") }
            } catch (error: Throwable) {
                val detail = error.message?.takeIf { it.isNotBlank() } ?: "Falha ao salvar o treino"
                mainHandler.post { callback.onResult(false, detail) }
            }
        }
    }

    @JvmStatic
    fun openSettings(context: Context) {
        try {
            val intent = HealthConnectClient.getHealthConnectManageDataIntent(
                context,
                HEALTH_CONNECT_PROVIDER
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Throwable) {
            openProviderStore(context)
        }
    }

    private fun openProviderStore(context: Context) {
        val provider = HEALTH_CONNECT_PROVIDER
        try {
            val uri = Uri.parse("market://details?id=$provider&url=healthconnect%3A%2F%2Fonboarding")
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setPackage("com.android.vending")
            )
        } catch (_: Throwable) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$provider")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
