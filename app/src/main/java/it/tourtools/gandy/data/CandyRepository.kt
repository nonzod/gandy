package it.tourtools.gandy.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.tourtools.gandy.data.model.Appliance
import it.tourtools.gandy.data.model.WashingMachineStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CandyRepository @Inject constructor(
    private val client: CandyClient,
    private val prefs: DataStore<Preferences>,
) {
    private val json = Json { ignoreUnknownKeys = true }

    val appliances: Flow<List<Appliance>> = prefs.data.map { p ->
        if (p[KEY_APPLIANCES].isNullOrBlank()) legacyAppliance(p) else currentList(p)
    }

    suspend fun addAppliance(appliance: Appliance) {
        prefs.edit { p ->
            p[KEY_APPLIANCES] = json.encodeToString(currentList(p) + appliance)
        }
    }

    suspend fun removeAppliance(id: String) {
        prefs.edit { p ->
            p[KEY_APPLIANCES] = json.encodeToString(currentList(p).filter { it.id != id })
        }
    }

    suspend fun readStatus(ip: String): WashingMachineStatus = client.readStatus(ip)

    suspend fun sendCommand(ip: String, params: Map<String, String>): String =
        client.sendCommand(ip, params)

    suspend fun provision(
        applianceIp: String,
        ssid: String,
        password: String,
        encryptKey: String,
    ): String = client.provision(applianceIp, ssid, password, encryptKey)

    private fun currentList(p: Preferences): List<Appliance> {
        val raw = p[KEY_APPLIANCES].orEmpty()
        return if (raw.isBlank()) emptyList() else json.decodeFromString(raw)
    }

    /** Migrazione dalla versione a dispositivo singolo, che salvava solo l'IP. */
    private fun legacyAppliance(p: Preferences): List<Appliance> {
        val legacyIp = p[KEY_IP].orEmpty()
        return if (legacyIp.isNotBlank()) {
            listOf(Appliance(name = CandyNetwork.DEFAULT_APPLIANCE_NAME, ip = legacyIp))
        } else {
            emptyList()
        }
    }

    private companion object {
        val KEY_IP = stringPreferencesKey("appliance_ip")
        val KEY_APPLIANCES = stringPreferencesKey("appliances")
    }
}
