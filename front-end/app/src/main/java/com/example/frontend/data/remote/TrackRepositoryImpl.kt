package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.domain.repositories.TrackRepository

class TrackRepositoryImpl(private val trackApi: TrackApi) : TrackRepository {

    companion object {
        private const val TAG = "TrackRepositoryImpl"
    }

    override suspend fun getTracks(): List<GetTracksResponse> {
        return try {
            Log.d(TAG, "🚀 Gọi API getTracks()...")
            val response = trackApi.getTracks()
            Log.d(TAG, "✅ API trả về ${response.size} bài hát")

            if (response.isNotEmpty()) {
                val first = response.first()
                Log.d(TAG, "🎵 Bài đầu tiên: title=${first.title}, cover=${first.coverImageKey}")
            } else {
                Log.w(TAG, "⚠️ Danh sách bài hát rỗng!")
            }

            response
        } catch (e: Exception) {
            Log.e(TAG, "💥 Lỗi khi gọi getTracks(): ${e.message}", e)
            emptyList()
        }
    }
}
