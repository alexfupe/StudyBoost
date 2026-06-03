package com.toka.studyboost.red

import com.toka.studyboost.datos.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ServicioRedApi {
    @POST("usuarios/registro")
    suspend fun registrarUsuario(@Body peticion: PeticionRegistro): Usuario

    @POST("usuarios/login")
    suspend fun iniciarSesion(@Body peticion: PeticionInicioSesion): Usuario

    @GET("apuntes")
    suspend fun obtenerApuntes(): List<Apunte>

    @GET("apuntes/{id}/resumen")
    suspend fun obtenerResumen(@Path("id") idApunte: String): Resumen

    @GET("apuntes/{id}/test")
    suspend fun obtenerTest(@Path("id") idApunte: String): List<PreguntaTest>

    @GET("usuarios/estadisticas")
    suspend fun obtenerEstadisticas(): Estadisticas

    @POST("usuarios/cambiar-contrasena")
    suspend fun cambiarContrasena(
        @Body datos: Map<String, String>
    ): Map<String, String>
}
