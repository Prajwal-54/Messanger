package com.example.messanger


import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messanger.DAO.PostDao
import com.example.messanger.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity(),IPosAdapter {

    private lateinit var adapter: PostsAdapter
    private lateinit var postdao : PostDao
    private lateinit var alertDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        val signOut = findViewById<Button>(R.id.signOutBtn)


        //add post listener
        fab.setOnClickListener {
            val intent = Intent(this,CreatePostActivity::class.java)
            startActivity(intent)
        }

        signOut.setOnClickListener {
            signOut()
        }

        setRecyclerView()

    }

    //recycler view
    private fun setRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        postdao = PostDao()
        val postCollection = postdao.postCollection
        val query = postCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        val posts = FirestoreRecyclerOptions.Builder<Post>().setQuery(query,Post::class.java).build()


        adapter = PostsAdapter(posts,this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = null

    }

    override fun onLiked(postId:String){
        postdao.updateLikes(postId)
    }


    private fun signOut(){

        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("")
        //set message for alert dialog
        builder.setMessage("Do you sign out from this account ? ")
        builder.setIcon(R.drawable.ic_baseline_login_24)


        builder.setPositiveButton("Yes"){dialogInterface, which ->
            Toast.makeText(applicationContext,"Signin out",Toast.LENGTH_LONG).show()
            Firebase.auth.signOut()

            val signActivity = Intent(this,SingInActivity::class.java)
            startActivity(signActivity)
            finish()
        }
        //performing cancel action
        builder.setNeutralButton("Cancel"){dialogInterface , which ->
            Toast.makeText(applicationContext,"clicked cancel\n operation cancel",Toast.LENGTH_LONG).show()
        }
        //performing negative action
        builder.setNegativeButton("No"){dialogInterface, which ->
            Toast.makeText(applicationContext,"Canceled sign out",Toast.LENGTH_LONG).show()
        }

        alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()



    }


    //for real time changes
    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }
    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

}