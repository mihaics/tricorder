package com.sysop.tricorder.sensor.rf.di

import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.rf.BleScanProvider
import com.sysop.tricorder.sensor.rf.CellularProvider
import com.sysop.tricorder.sensor.rf.WifiScanProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class RfModule {

    @Binds @IntoSet
    abstract fun bindWifiProvider(impl: WifiScanProvider): SensorProvider

    @Binds @IntoSet
    abstract fun bindBleProvider(impl: BleScanProvider): SensorProvider

    @Binds @IntoSet
    abstract fun bindCellularProvider(impl: CellularProvider): SensorProvider

    companion object {
        @Provides
        fun provideWifiManager(@ApplicationContext context: Context): WifiManager? =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        @Provides
        fun provideBluetoothManager(@ApplicationContext context: Context): BluetoothManager? =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

        @Provides
        fun provideTelephonyManager(@ApplicationContext context: Context): TelephonyManager? =
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    }
}
