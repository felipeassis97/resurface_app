package com.resurface.resurface.permission

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.resurface.resurface.service.ResurfaceAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fonte única do status de permissão, sempre lido AO VIVO do OS (nunca de flag persistido, G3):
 * o usuário pode revogar nas configurações fora do app.
 */
@Singleton
class PermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Status ao vivo de uma permissão. */
    fun isGranted(permission: AppPermission): Boolean = when (permission) {
        AppPermission.USAGE_ACCESS -> isUsageAccessGranted()
        AppPermission.NOTIFICATIONS -> isNotificationsGranted()
        AppPermission.ACCESSIBILITY -> isAccessibilityEnabled()
    }

    /** Status ao vivo de todas as permissões obrigatórias. */
    fun statuses(): Map<AppPermission, Boolean> =
        AppPermission.required.associateWith { isGranted(it) }

    /** Verdadeiro se todas as obrigatórias estão concedidas. */
    fun allRequiredGranted(): Boolean = AppPermission.required.all { isGranted(it) }

    /** Intent pra tela de concessão de uma permissão especial; null pra runtime (usa o diálogo). */
    fun settingsIntent(permission: AppPermission): Intent? = when (permission) {
        AppPermission.USAGE_ACCESS -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        AppPermission.ACCESSIBILITY -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        AppPermission.NOTIFICATIONS -> null
    }

    /** Acessibilidade ligada = nosso serviço está na lista de serviços habilitados (ao vivo). */
    private fun isAccessibilityEnabled(): Boolean {
        val expected = ComponentName(context, ResurfaceAccessibilityService::class.java).flattenToString()
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':').apply { setString(enabled) }
        for (component in splitter) {
            if (component.equals(expected, ignoreCase = true)) return true
        }
        return false
    }

    /** Checa o acesso ao uso via AppOps (não dá pra usar checkSelfPermission aqui). */
    private fun isUsageAccessGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** Checa POST_NOTIFICATIONS (só existe na API 33+; abaixo é sempre concedida). */
    private fun isNotificationsGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
