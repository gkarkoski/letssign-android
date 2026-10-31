package com.example.challenge_letssign.utils

import java.net.Inet4Address
import java.net.NetworkInterface

object IpAddressResolver {
    private const val FALLBACK_LOCAL_IP = "192.168.1.100"

    fun resolveLocalIpAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            for (networkInterface in interfaces) {
                if (!networkInterface.isUp || networkInterface.isLoopback) continue
                val addresses = networkInterface.inetAddresses?.toList().orEmpty()
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        val hostAddress = address.hostAddress
                        if (!hostAddress.isNullOrBlank()) {
                            return hostAddress
                        }
                    }
                }
            }
            FALLBACK_LOCAL_IP
        } catch (_: Exception) {
            FALLBACK_LOCAL_IP
        }
    }
}
