package com.example.settingsapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
}