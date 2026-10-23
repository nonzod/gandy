package it.nicolatomassoni.gandy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApplianceQrData(
    val v: Int = 1,
    val name: String,
    val ip: String,
    val xorKey: String = "",
) {
    fun toAppliance() = Appliance(name = name, ip = ip, xorKey = xorKey)

    companion object {
        fun from(appliance: Appliance) = ApplianceQrData(
            name = appliance.name,
            ip = appliance.ip,
            xorKey = appliance.xorKey,
        )
    }
}
