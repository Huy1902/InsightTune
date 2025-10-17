package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.data.models.song.PlayingRequest
import com.example.frontend.data.models.song.PlayingResponse
import com.example.frontend.domain.repositories.PlayingRepository

class PlayingRepositoryImpl(
    private val playingApi: PlayingApi,
) : PlayingRepository {
    override suspend fun getUrlTrack(storageKey: String, coverImageKey: String?): PlayingResponse {
        try { // Bọc trong try-catch để an toàn hơn với lỗi mạng
            val request = PlayingRequest(storageKey, coverImageKey)
            Log.d("PLAYING", "Sending playing request: $request")
            val response = playingApi.getPlaying(request)
            Log.d("PLAYING", "HTTP status: ${response.code()} - ${response.message()}")

            if (response.isSuccessful) {
                val body = response.body()
                // Nếu body là null hoặc trackUrl rỗng, cũng coi như thất bại
                if (body != null && body.trackUrl.isNotEmpty()) {
                    return body // Trả về dữ liệu nếu thành công
                } else {
                    Log.e("PLAYING", "Response successful but body is invalid.")
                }
            } else {
                Log.e("PLAYING", "HTTP Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PLAYING", "An exception occurred", e)
        }
        return PlayingResponse("", "") // Trả về dữ liệu mặc định nếu có lỗi

    }
}