package com.example.messanger.DAO

import com.example.messanger.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {
    private val db = Firebase.firestore
    private val userCollection = db.collection("users")


     fun addUser(user : User?){
         GlobalScope.launch(Dispatchers.IO) {
             user?.let {
                 userCollection.document(user.uid).set(it)
             }
         }

    }

    fun getUserById(uId : String): Task<DocumentSnapshot>{
        return userCollection.document(uId).get()
    }

}