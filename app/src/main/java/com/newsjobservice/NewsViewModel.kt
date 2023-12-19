package com.newsjobservice

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newsjobservice.db.AppDatabase
import com.newsjobservice.db.NewsDao
import com.newsjobservice.db.NewsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsViewModel(applicationContext: Application) : AndroidViewModel(application = applicationContext){

    var newsDao:NewsDao = AppDatabase.getInstance(applicationContext).newsDao()

    private var _list = MutableStateFlow<List<NewsEntity>>(emptyList())
    val list = _list.asStateFlow()

    init {
        getAllNews()
    }

    fun getAllNews(){
        viewModelScope.launch {
            Log.d("TAG", "getAllNews: "+     newsDao.getAllNews())
            withContext(Dispatchers.Main){
                _list.value=newsDao.getAllNews()
            }
        }
    }
}