package com.newsjobservice.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.newsjobservice.R
import com.newsjobservice.data.Config.API_KEY
import com.newsjobservice.data.Config.BASE_URL
import com.newsjobservice.data.NewsApiService
import com.newsjobservice.data.NewsResponse
import com.newsjobservice.db.AppDatabase
import com.newsjobservice.db.NewsEntity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.util.Date

class NewsSyncJobService : JobService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.e("TAG", "API onStartJob: ")
        // Perform background task (make API call, save data, and show notifications)
        performApiCall(params)
        return true // Job is still doing work in a separate thread
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.e("TAG", "API onStopJob: ")
        // Job needs to be rescheduled
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(DelicateCoroutinesApi::class)
    private fun performApiCall(jobParameters: JobParameters?) {
        Log.e("TAG", "API BASE_URL launch:$BASE_URL ")
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(NewsApiService::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            Log.e("TAG", "API call launch: ")
            try {
                val response: Response<NewsResponse> = apiService.getTopHeadlines("in", API_KEY)
                if (response.isSuccessful) {
                    val newsList = response.body()?.articles?.map {
                        NewsEntity(title = it.title, description = it.description, url = it.url)
                    } ?: emptyList()
                    Log.e("TAG", "API call response:  $newsList")
                    // Save data to the local database
                    val newsDao = AppDatabase.getInstance(applicationContext).newsDao()
                    newsDao.insertAll(newsList)

                    // Show notification
                    showNotification("News Synced", "New articles are available."+ LocalDateTime.now())
                } else {
                    Log.e("TAG", "API call failed. Code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("TAG", "Exception during API call: ${e.message}")
            } finally {
                jobFinished(jobParameters, false) // Job is complete, reschedule if needed
            }
        }
    }

    private fun showNotification(title: String, content: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "news_channel",
                "News Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "news_channel")
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}