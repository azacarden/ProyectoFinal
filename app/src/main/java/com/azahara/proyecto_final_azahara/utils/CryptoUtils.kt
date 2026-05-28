package com.azahara.proyecto_final_azahara.utils

import java.security.MessageDigest

object CryptoUtils {

     //Aplica el algoritmo criptográfico SHA-256 a un texto plano.
     //Devuelve una cadena hexadecimal única e irreversible.
    fun sha256(texto: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}