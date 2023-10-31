package com.example.settingsapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.settingsapp.databinding.ActivitySettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Funcion de extensión
val Context.datastore: DataStore<Preferences> by preferencesDataStore(name="settings")
// el by(delegado)permite crear una unica instancia de la base de datos
class SettingsActivity : AppCompatActivity() {

    companion object {
        const val VOLUME_LVL = "volume_lvl"
        const val KEY_BLUETOOTH = "key_bluetooth"
        const val KEY_VIBRATION = "key_vibration"
        const val KEY_DARKMODE = "key_darkmode"
    }

    private lateinit var binding: ActivitySettingsBinding
    private var firstTime:Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nos colgamos de getSettings en una corrutina (hilo secudario)
        // para que nos avise de algun cambio
        CoroutineScope(Dispatchers.IO).launch {
            // Ejecuta esto solo cuando firstTime es true
            getSettings().filter { firstTime } .collect{ settingsModel ->
                if (settingsModel != null){
                    // se usa runOnUi... porque no se puede alterar interfaz desde hilo secundario
                    runOnUiThread {
                        // datos SettingsModel
                        binding.switchVibration.isChecked = settingsModel.vibration
                        binding.switchDarkMode.isChecked = settingsModel.darkMode
                        binding.switchBluetooth.isChecked = settingsModel.bluetooth
                        binding.rsVolume.setValues(settingsModel.volume.toFloat())
                        firstTime = !firstTime
                    }

                }
            }
        }



        initUI()
    }

    private fun initUI() {
        binding.rsVolume.addOnChangeListener {_,value,_ ->
            // Corrutina pra poder llamar a funcion que tiene suspend (funciona en hilo secundario)
            CoroutineScope(Dispatchers.IO).launch {
                saveVolume(value.toInt())
            }
        }

        binding.switchBluetooth.setOnCheckedChangeListener{_, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveOption(KEY_BLUETOOTH,value)
            }
        }
        binding.switchVibration.setOnCheckedChangeListener{_, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveOption(KEY_VIBRATION,value)
            }
        }

        binding.switchDarkMode.setOnCheckedChangeListener{_, value ->
            if(value) enableDarkMode() else disableDarkMode()
            CoroutineScope(Dispatchers.IO).launch {
                saveOption(KEY_DARKMODE,value)
            }
        }
    }

    // suspend porque va a trabajar en una corutina
    private suspend fun saveVolume(value:Int) {
        datastore.edit{ preference ->
            preference[intPreferencesKey(VOLUME_LVL)] = value
        }
    }

    private suspend fun saveOption(key: String, value: Boolean){
        datastore.edit { preference ->
            preference[booleanPreferencesKey(key)] = value
        }
    }

    // Se usa una dataclass para poder devolver las configuraciones usando FLOW
    private fun getSettings(): Flow<SettingsModel> {
        return datastore.data.map { preference ->
            SettingsModel(
                volume = preference[intPreferencesKey(VOLUME_LVL)] ?: 50,
                bluetooth = preference[booleanPreferencesKey(KEY_BLUETOOTH)] ?: false,
                darkMode = preference[booleanPreferencesKey(KEY_DARKMODE)] ?: false,
                vibration = preference[booleanPreferencesKey(KEY_VIBRATION)] ?: false
            )
        }
    }

    private fun enableDarkMode(){
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        delegate.applyDayNight()
    }

    private fun disableDarkMode(){
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        delegate.applyDayNight()
    }
}