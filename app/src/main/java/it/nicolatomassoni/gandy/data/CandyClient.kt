package it.nicolatomassoni.gandy.data

import it.nicolatomassoni.gandy.data.CandyProtocol.Param
import it.nicolatomassoni.gandy.data.model.ApplianceResponse
import it.nicolatomassoni.gandy.data.model.WashingMachineStatus
import it.nicolatomassoni.gandy.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CandyClient @Inject constructor(
    private val http: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private var xorKey: String? = null

    suspend fun readStatus(ip: String): WashingMachineStatus = withContext(ioDispatcher) {
        val body = get(url(ip, CandyProtocol.PATH_READ, "${CandyProtocol.QUERY_ENCRYPTED}=0"))
        val cleaned = body.replace(TRAILING_COMMA, "$1")
        val response = json.decodeFromString<ApplianceResponse>(cleaned)
        response.statusLavatrice ?: throw CandyException.NoStatusData()
    }

    suspend fun recoverKey(ip: String): String = withContext(ioDispatcher) {
        val hex = get(url(ip, CandyProtocol.PATH_READ, "${CandyProtocol.QUERY_ENCRYPTED}=1"))
        val raw = hex.trim().decodeHex()
        val keyLen = CandyProtocol.XOR_KEY_LENGTH
        if (raw.size < keyLen + MIN_PAYLOAD_AFTER_KEY) throw CandyException.ResponseTooShort()
        for (prefix in CandyProtocol.STATUS_PLAINTEXT_PREFIXES) {
            val pb = prefix.toByteArray()
            if (pb.size < keyLen) continue
            val candidate = ByteArray(keyLen) { i -> (raw[i].toInt() xor pb[i].toInt()).toByte() }
            val keyStr = String(candidate)
            if (keyStr.all { it.isLetterOrDigit() }) {
                xorKey = keyStr
                return@withContext keyStr
            }
        }
        throw CandyException.KeyRecoveryFailed()
    }

    suspend fun sendCommand(ip: String, params: Map<String, String>): String =
        withContext(ioDispatcher) {
            val key = xorKey ?: recoverKey(ip)
            val plaintext = params.entries.joinToString("&") { "${it.key}=${it.value}" }
            val encrypted = xorEncrypt(plaintext, key)
            val query = "${CandyProtocol.QUERY_ENCRYPTED}=1&${CandyProtocol.QUERY_DATA}=$encrypted"
            val responseHex = get(url(ip, CandyProtocol.PATH_WRITE, query))
            xorDecrypt(responseHex, key)
        }

    suspend fun provision(
        applianceIp: String,
        ssid: String,
        password: String,
        encryptKey: String,
    ): String = withContext(ioDispatcher) {
        val query = "${CandyProtocol.QUERY_ENCRYPTED}=0" +
            "&${Param.NETWORK_MODE}=${CandyProtocol.NETWORK_MODE_STATION}" +
            "&${Param.SSID}=${URLEncoder.encode(ssid, Charsets.UTF_8.name())}" +
            "&${Param.PASSWORD}=${URLEncoder.encode(password, Charsets.UTF_8.name())}" +
            "&${Param.ENCRYPT_KEY}=$encryptKey"
        get(url(applianceIp, CandyProtocol.PATH_CONFIG, query))
    }

    private fun xorEncrypt(plaintext: String, key: String): String {
        val keyBytes = key.toByteArray()
        return plaintext.toByteArray().mapIndexed { i, b ->
            (b.toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
        }.toByteArray().toHex()
    }

    private fun xorDecrypt(hex: String, key: String): String {
        val raw = hex.trim().decodeHex()
        val keyBytes = key.toByteArray()
        return String(ByteArray(raw.size) { i -> (raw[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte() })
    }

    private fun String.decodeHex(): ByteArray {
        val s = this.trim()
        return ByteArray(s.length / 2) { i -> s.substring(i * 2, i * 2 + 2).toInt(HEX_RADIX).toByte() }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02X".format(it) }

    private fun url(ip: String, path: String, query: String) = "http://$ip/$path?$query"

    private fun get(url: String): String {
        val request = Request.Builder().url(url).header("Connection", "close").build()
        try {
            return http.newCall(request).execute().use { response ->
                response.body?.string() ?: throw CandyException.EmptyResponse()
            }
        } catch (e: SocketTimeoutException) {
            throw CandyException.Timeout(e)
        } catch (e: ConnectException) {
            throw CandyException.ConnectionRefused(e)
        } catch (e: UnknownHostException) {
            throw CandyException.UnknownHost(e)
        }
    }

    private companion object {
        /** Il firmware produce JSON con virgole finali prima di } o ]. */
        val TRAILING_COMMA = Regex(",\\s*([}\\]])")
        const val MIN_PAYLOAD_AFTER_KEY = 4
        const val HEX_RADIX = 16
    }
}
