package com.example.frontend.data.remote

import com.example.frontend.core.AppPreferences
import com.example.frontend.core.AuthInterceptor
import com.example.frontend.core.Constants
import com.example.frontend.data.models.user.GoogleResponse
import com.example.frontend.data.models.user.GoogleResponseResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import kotlin.getValue
import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneId
import java.util.*

object ApiClient {

    private lateinit var prefs: AppPreferences
    private val mainClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(prefs) { refreshAuthApi }) // Sửa ở đây
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

    private val refreshClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val gatewayRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(mainClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val refreshRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(refreshClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    val authApi: AuthApi by lazy {
        gatewayRetrofit.create(AuthApi::class.java)
    }
    private val refreshAuthApi: AuthApi by lazy {
        refreshRetrofit.create(AuthApi::class.java)
    }

    val userApi: UserApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.USER_SERVICE_BASE_URL)
            .client(mainClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }

    val trackApi: TrackApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.TRACK_SERVICE_BASE_URL)
            .client(mainClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrackApi::class.java)
    }

    val playingApi: PlayingApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.PLAYING_SERVICE_BASE_URL)
            .client(mainClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlayingApi::class.java)
    }

    val historyApi: HistoryApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.HISTORY_SERVICE_BASE_URL)
            .client(mainClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(HistoryApi::class.java)
    }

    val chatbotApi: ChatbotApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.CHATBOT_URL)
            .client(mainClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ChatbotApi::class.java)
    }

    val favoriteApi: FavoriteApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.FAVORITE_SERVICE_BASE_URL)
            .client(mainClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(FavoriteApi::class.java)
    }

    fun init(prefs: AppPreferences) {
        this.prefs = prefs
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
    val googleAuthApi: GoogleAuthApi by lazy {
        gatewayRetrofit.create(GoogleAuthApi::class.java)
    }
}

