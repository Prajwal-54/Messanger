package com.example.messanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.example.messanger.DAO.UserDao
import com.example.messanger.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class SingInActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

     private  lateinit  var signInBtn: SignInButton
     private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_in)

         signInBtn   = findViewById(R.id.signInBtn)
         progressBar = findViewById(R.id.progressBar)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
        signInBtn.setOnClickListener{
            signIn()
        }



    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUi(currentUser)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
        signInBtn.visibility= View.GONE
        progressBar.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String?) {

        if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken,null)

                GlobalScope.launch  (Dispatchers.IO){
                    val auth = auth.signInWithCredential(credential).await()
                    val user = auth.user

                    withContext(Dispatchers.Main){
                        updateUi(user)
                    }

                }
        }


    }

    private fun updateUi(user: FirebaseUser?) {

        if(user != null){
            val newUser = User(user.uid,user.displayName.toString(),user.photoUrl.toString())
            val userDao = UserDao()
            userDao.addUser(newUser)
            progressBar.visibility = View.GONE
            val mainActivity = Intent(this,MainActivity::class.java)
            startActivity(mainActivity)
            finish()
        }
        else
        {
            signInBtn.visibility= View.VISIBLE
            progressBar.visibility = View.GONE
        }

    }


    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}