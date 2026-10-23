package it.nicolatomassoni.gandy.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Appliance(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val ip: String,
    val xorKey: String = "",
)
