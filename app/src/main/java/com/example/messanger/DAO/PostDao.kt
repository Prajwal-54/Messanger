package com.example.messanger.DAO


import android.net.Uri
import com.example.messanger.models.Post
import com.example.messanger.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.scottyab.aescrypt.AESCrypt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class PostDao {
    private val db = Firebase.firestore
     val postCollection = db.collection("posts")
    private var storageRef = FirebaseStorage.getInstance()
    private val auth = Firebase.auth



    fun addPost(text : String,hasImage : Boolean,imageUri : Uri?){
        val currentUserId = auth.currentUser!!.uid

        GlobalScope.launch {

            val userDao  = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            val currentTime  = System.currentTimeMillis()

            if( hasImage) {
                val storage = storageRef.getReference("image/$currentTime")
                if (imageUri != null) {
                    storage.putFile(imageUri).addOnSuccessListener {

                    }
                }
            }
            //encrypt msg
            val encrypted = AESCrypt.encrypt(currentUserId, text)!!
            val post = Post(encrypted, user, currentTime, hasImage)
            postCollection.document().set(post)
        }

    }


    fun getPostById(postId: String): Task<DocumentSnapshot> {
        return postCollection.document(postId).get()
    }

    fun updateLikes(postId: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)!!
            val isLiked = post.likedBy.contains(currentUserId)

            if(isLiked) {
                post.likedBy.remove(currentUserId)
            } else {
                post.likedBy.add(currentUserId)
            }
            postCollection.document(postId).set(post)
        }

    }

}