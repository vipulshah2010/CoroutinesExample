package com.example.vipul.coroutinesexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

class MainActivity : AppCompatActivity() {

    private val uiContext: CoroutineContext = UI
    private val bgContext: CoroutineContext = CommonPool
    private lateinit var job: Job;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        job = launch(uiContext) {
            val result = withTimeoutOrNull(12, TimeUnit.SECONDS) {
                withContext(bgContext) {
                    getLatestMovies()
                }
            }

            Toast.makeText(this@MainActivity, result, Toast.LENGTH_LONG).show()
        }

        job.invokeOnCompletion {
            Toast.makeText(this, "Completed!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        if (job.isActive) {
            job.cancel()
        }
        super.onDestroy()
    }

    private suspend fun getLatestMovies(): String {
        val urlString = "https://api.themoviedb.org/3/movie/now_playing?api_key=55957fcf3ba81b137f8fc01ac5a31fb5&language=en-US";
        val url = URL(urlString)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.connect()
        delay(5000)
        return connection.inputStream.toResponseString()
    }

    private fun InputStream.toResponseString(): String {
        val bufferSize = 1024
        val buffer = CharArray(bufferSize)
        val out = StringBuilder()
        val streamReader = InputStreamReader(this, "UTF-8")
        while (true) {
            val rsz = streamReader.read(buffer, 0, buffer.size)
            if (rsz < 0)
                break
            out.append(buffer, 0, rsz)
        }
        return out.toString()
    }
}
