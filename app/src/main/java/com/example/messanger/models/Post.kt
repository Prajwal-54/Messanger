package com.example.messanger.models

data class Post(val post : String = "",val createdBy : User = User() , val createdAt : Long = 0 ,val hasImage : Boolean = false, val likedBy : ArrayList<String> = ArrayList())
