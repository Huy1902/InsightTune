package com.example.frontend.data.remote

import com.example.frontend.core.AppPreferences
import com.example.frontend.core.AuthInterceptor
import com.example.frontend.core.Constants
import com.example.frontend.data.register.GoogleResponseResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import java.util.*
import java.util.concurrent.TimeUnit

object ApiClient {

    private lateinit var prefs: AppPreferences

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val mainClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(prefs) { refreshAuthApi }) // Sửa ở đây
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ ->
            val dateStr = json.asString
            try {
                val normalized = if (dateStr.endsWith("Z") || dateStr.contains("+")) {
                    dateStr
                } else {
                    "${dateStr}Z"
                }

                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX", Locale.getDefault())
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                sdf.parse(normalized)
            } catch (e: Exception) {
                null
            }
        })
        .create()

    private val gatewayRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(mainClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val refreshClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val refreshAuthApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(refreshClient) // ✅ client riêng, không interceptor
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApi::class.java)
    }


    val authApi: AuthApi by lazy { gatewayRetrofit.create(AuthApi::class.java) }
    val userApi: UserApi by lazy { gatewayRetrofit.create(UserApi::class.java) }
    val trackApi: TrackApi by lazy { gatewayRetrofit.create(TrackApi::class.java) }
    val playingApi: PlayingApi by lazy { gatewayRetrofit.create(PlayingApi::class.java) }
    val historyApi: HistoryApi by lazy { gatewayRetrofit.create(HistoryApi::class.java) }
    val favoriteApi: FavoriteApi by lazy { gatewayRetrofit.create(FavoriteApi::class.java) }

    val chatbotApi: ChatbotApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.CHATBOT_URL)
            .client(mainClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ChatbotApi::class.java)
    }

    val googleAuthApi: GoogleAuthApi by lazy {
        gatewayRetrofit.create(GoogleAuthApi::class.java)
    }

    suspend fun loginWithGoogleIdToken(idToken: String): GoogleResponseResult {
        val response = googleAuthApi.verifyIdToken(mapOf("idToken" to idToken))

        if (response.isSuccessful && response.body() != null) {
            val apiResponse = response.body()!!
            if (apiResponse.code == 200 && apiResponse.result?.token != null) {
                return apiResponse.result
            } else {
                throw Exception(apiResponse.message ?: "Backend returned a successful status but with an error.")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            throw Exception("Backend error ${response.code()}: $errorBody")
        }
    }

    fun init(prefs: AppPreferences) {
        this.prefs = prefs
    }
}
