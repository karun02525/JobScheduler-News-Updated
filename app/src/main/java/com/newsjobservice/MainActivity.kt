package com.newsjobservice

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.newsjobservice.db.NewsEntity
import com.newsjobservice.service.NewsSyncJobService
import com.newsjobservice.ui.theme.NewsJobServiceTheme

class MainActivity : ComponentActivity() {

    companion object{
        var NEWS_SYNC_JOB_ID=23432
    }
    private lateinit var viewmodel:NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel= ViewModelProvider(this)[NewsViewModel::class.java]
        setContent {
            NewsJobServiceTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {


                    Column {

                        Button(onClick = { startTask() }) {
                            Text(text = "Star service")
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(onClick = { stop() }) {
                            Text(text = "Stop service")
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        ListNews(viewmodel)
                    }

                }
            }
        }
    }



    fun stop(){
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
       jobScheduler.cancel(NEWS_SYNC_JOB_ID)
    }

    fun startTask(){
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(this, NewsSyncJobService::class.java)

        val jobInfo = JobInfo.Builder(NEWS_SYNC_JOB_ID, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
          //  .setPeriodic(24 * 60 * 60 * 1000) // Set to run every 24 hours
            .setPeriodic(15 * 60 * 1000) // Set to run every 15 min
           // .setPeriodic(24 * 60 * 60 * 1000) // Set to run every 24 hours
            .setPersisted(true)
            .build()

        val resultCode = jobScheduler.schedule(jobInfo)

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("TAG", "Job scheduled successfully")
        } else {
            Log.e("TAG", "Job scheduling failed")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


@Composable
fun ListNews(viewModel: NewsViewModel) {
    val listNews by viewModel.list.collectAsState(emptyList())
    LazyColumn(){
        items(listNews){
            Adapter(it)

        }
    }
}

@Composable
fun Adapter(model: NewsEntity) {
    Column(
        modifier = Modifier.padding(10.dp),
        verticalArrangement=Arrangement.Center,
        horizontalAlignment=Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.heightIn(4.dp))
        Text(text = ""+model.id)
        Spacer(modifier = Modifier.heightIn(4.dp))
        Text(text = ""+model.title)
        Spacer(modifier = Modifier.heightIn(4.dp))
        Text(text = ""+model.description)
        Spacer(modifier = Modifier.heightIn(4.dp))
        Text(text = ""+model.url)
        Spacer(modifier = Modifier.heightIn(4.dp))

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NewsJobServiceTheme {
        Greeting("Android")
    }
}