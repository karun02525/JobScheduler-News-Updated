package com.newsjobservice.data

data class NewsResponse(val articles: List<Article>)

data class Article(val title: String, val description: String, val url: String)