package com.resurface.resurface.di

import com.resurface.resurface.ble.AlertHaptics
import com.resurface.resurface.ble.BluetoothEnvironment
import com.resurface.resurface.ble.HapticSender
import com.resurface.resurface.ble.WristbandAlertHaptics
import com.resurface.resurface.ble.WristbandGattClient
import com.resurface.resurface.ble.WristbandLink
import com.resurface.resurface.ble.WristbandRepository
import com.resurface.resurface.ble.WristbandScanner
import com.resurface.resurface.data.wristband.RememberedWristbandStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Bindings BLE da pulseira. Env/scanner/gatt/stores têm @Inject próprio; só o repositório é
 * provido explícito (o dispatcher tem default que o Dagger não enxerga). Tudo @Singleton — o
 * link precisa sobreviver a qualquer tela, e um segundo GATT client causa status 133 repetido.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BleModule {

    /** Liga o seam de vibração ao impl que fala com a pulseira. */
    @Binds
    @Singleton
    abstract fun bindAlertHaptics(impl: WristbandAlertHaptics): AlertHaptics

    companion object {
        /** Dono @Singleton do link BLE (dispatcher default omitido de propósito). */
        @Provides
        @Singleton
        fun provideWristbandRepository(
            environment: BluetoothEnvironment,
            scanner: WristbandScanner,
            gattClient: WristbandGattClient,
            rememberedStore: RememberedWristbandStore,
        ): WristbandRepository = WristbandRepository(
            environment = environment,
            scanner = scanner,
            gattClient = gattClient,
            rememberedStore = rememberedStore,
        )

        /** Expõe o seam de envio a partir do mesmo repositório singleton. */
        @Provides
        @Singleton
        fun provideHapticSender(repository: WristbandRepository): HapticSender = repository

        /** Expõe a superfície de link (UI/serviço) a partir do mesmo repositório. */
        @Provides
        @Singleton
        fun provideWristbandLink(repository: WristbandRepository): WristbandLink = repository
    }
}
