package com.resurface.resurface.dev

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel das ferramentas de dev (isolado): só expõe o disparo do aviso de teste. */
@HiltViewModel
class DevToolsViewModel @Inject constructor(
    private val trigger: TestAlertTrigger,
) : ViewModel() {

    /** Dispara o aviso de teste (fire-and-forget no escopo do VM). */
    fun onTestAlert() {
        viewModelScope.launch { trigger.fire() }
    }
}
