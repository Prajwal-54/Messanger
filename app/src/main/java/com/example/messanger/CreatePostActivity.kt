package com.example.messanger

import android.content.Intent
import android.net.Uri
import android.os.Bundle

import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.messanger.DAO.PostDao
import com.example.messanger.DAO.UserDao
import com.example.messanger.models.Post
import com.example.messanger.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.scottyab.aescrypt.AESCrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class CreatePostActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val postCollection = db.collection("posts")
    private var storageRef = FirebaseStorage.getInstance()
    private val auth = Firebase.auth

    private lateinit var postDao : PostDao
    private val SELECT_IMAGE : Int = 100
    private lateinit var    inputImage :ImageView
    private lateinit var galleryimage : ImageView
    private lateinit var  postButton :Button
    private lateinit var createPost:EditText
    private lateinit var createPostLoader : ProgressBar
    private  var hasImage : Boolean = false
    private lateinit var selectedImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

         createPost = findViewById(R.id.postText)
         postButton = findViewById(R.id.postButton)
         inputImage = findViewById(R.id.inputImage)
         galleryimage = findViewById(R.id.galleryImage)
         hasImage = false
        createPostLoader = findViewById(R.id.createPostLoader)


        postDao = PostDao()

        inputImage.setOnClickListener {
                getPhotoFromGallery()
        }

        postButton.setOnClickListener {
            val inputPost = createPost.text.toString().trim()

            if(inputPost.isNotEmpty() || hasImage){
//                createPostLoader.visibility = View.VISIBLE

                if (inputPost.isNotEmpty() && !hasImage)
                {
//                    postDao.addPost(inputPost,hasImage,null)
                    addPost(inputPost,hasImage,null)

                }
                else{
//                    postDao.addPost(inputPost,hasImage,selectedImageUri)
                    addPost(inputPost,hasImage,selectedImageUri)

                }
//                createPostLoader.visibility= View.GONE

          }
            else {
                Toast.makeText(applicationContext,"Cannot send empty messsage !!",Toast.LENGTH_LONG).show()
            }


        }
    }

    private fun getPhotoFromGallery() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && data != null){
            if(requestCode == SELECT_IMAGE){
                 selectedImageUri = data.data!!
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                        hasImage= true

                    galleryimage.visibility = View.VISIBLE
                    galleryimage.setImageURI(selectedImageUri)


                }
                else {
                    hasImage = false
                }
            }
            else {
                hasImage = false
            }
        }
    }



    private fun addPost(text : String, hasImage : Boolean, imageUri : Uri?){
        val currentUserId = auth.currentUser!!.uid
        createPostLoader.visibility = View.VISIBLE


        GlobalScope.launch {

            val userDao  = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            val currentTime  = System.currentTimeMillis()
//            //encrypt msg
//            val encrypted = AESCrypt.encrypt(currentUserId, text)!!

            if( hasImage) {
                val storage = storageRef.getReference("image/$currentTime")
                if (imageUri != null) {
                    storage.putFile(imageUri).addOnSuccessListener {
                        val post = Post(text, user, currentTime, hasImage)
                        postCollection.document().set(post)

                        runOnUiThread(Runnable {
                            createPostLoader.visibility = View.GONE
                        })
                        val intt = Intent(this@CreatePostActivity,MainActivity::class.java)
                        startActivity(intt)


                    }.removeOnFailureListener{
                        runOnUiThread(Runnable {
                            Toast.makeText(applicationContext,"post not sent !! \n failed",Toast.LENGTH_LONG).show()
                            createPostLoader.visibility = View.GONE
                        })
                    }
                }
                else{
                    runOnUiThread(Runnable {
                        Toast.makeText(applicationContext,"post not sent !! \n failed",Toast.LENGTH_LONG).show()
                        createPostLoader.visibility = View.GONE
                    })
                }

            }
            else{
                val post = Post(text, user, currentTime, hasImage)
                postCollection.document().set(post).await()
                runOnUiThread(Runnable {
                    createPostLoader.visibility = View.GONE
                })
                val intt = Intent(this@CreatePostActivity,MainActivity::class.java)
                startActivity(intt)

            }


        }

    }




}