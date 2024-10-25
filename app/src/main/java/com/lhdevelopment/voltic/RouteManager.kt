package com.lhdevelopment.voltic

import android.content.Context
import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RouteManager(private val apiService: ApiService) {

    fun sendRouteData(userId: Int, startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double, context: Context) {
        val fullUrl = "https://ykyoaekhp9.execute-api.us-east-1.amazonaws.com/Stage1/route/"  // Cambia esto por tu URL

        // Crear el objeto JSON para el cuerpo
        val routeJsonObject = JSONObject().apply {
            put("ID_Usuario", userId)
            put("Inicio_Latitud", startLatitude)
            put("Inicio_Longitud", startLongitude)
            put("Fin_Latitud", endLatitude)
            put("Fin_Longitud", endLongitude)
        }

        Log.d("RouteData", "Enviando datos: $routeJsonObject")  // Se eliminó el toString()

        // Enviar el objeto JSON como cuerpo en la solicitud
        apiService.sendRouteData(fullUrl, routeJsonObject.toString()).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("RouteData", "Datos de la ruta enviados exitosamente.")
                    Toast.makeText(context, "Ruta guardada exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("RouteData", "Error al enviar los datos de la ruta: ${response.code()}")
                    Toast.makeText(context, "Error al guardar la ruta", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("RouteData", "Fallo en la solicitud: ${t.message}", t)
                Toast.makeText(context, "Error en la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
