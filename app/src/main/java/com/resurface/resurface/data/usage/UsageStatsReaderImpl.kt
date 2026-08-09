package com.resurface.resurface.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import com.resurface.resurface.di.IoDispatcher
import com.resurface.resurface.domain.model.Config
import com.resurface.resurface.domain.model.UsageEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Impl real do [UsageStatsReader] sobre o `UsageStatsManager`. Lê o stream de eventos
 * (`queryEvents`, fresco em ~1 s — GAPS G2), nunca agregados. Alvos fixos no F1 (D19).
 */
class UsageStatsReaderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
) : UsageStatsReader {

    private val targets: Set<String> = Config().targetPackages

    /** Consulta a janela, mapeia cada evento e devolve os de domínio ordenados por tempo. */
    override suspend fun events(from: Long, to: Long): List<UsageEvent> = withContext(io) {
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stream = manager.queryEvents(from, to)
        val out = ArrayList<UsageEvent>()
        val event = UsageEvents.Event()
        while (stream.getNextEvent(event)) {
            UsageEventMapper.map(event.eventType, event.packageName ?: "", event.timeStamp, targets)
                ?.let(out::add)
        }
        out.sortBy { it.timestamp }
        out
    }

    /** Lê o AppOps ao vivo — o usuário pode revogar o acesso fora do app a qualquer momento. */
    override fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
