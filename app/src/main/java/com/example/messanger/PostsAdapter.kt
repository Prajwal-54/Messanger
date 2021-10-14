package com.example.messanger

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messanger.DAO.PostDao

import com.example.messanger.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

import com.scottyab.aescrypt.AESCrypt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class PostsAdapter(options: FirestoreRecyclerOptions<Post>,val listener : IPosAdapter) : FirestoreRecyclerAdapter<Post, PostsAdapter.PostHolder>(
    options
) {

   inner class PostHolder(item : View) : RecyclerView.ViewHolder(item){
        val postImage : ImageView =  item.findViewById(R.id.userImage)
        val postUserName : TextView =  item.findViewById(R.id.userName)
        val postCreatedAt : TextView =  item.findViewById(R.id.createAt)
        val postTitle : TextView =  item.findViewById(R.id.postTitle)
        val postLikeBtn : ImageView =  item.findViewById(R.id.likeButton)
        val postLikeCount : TextView =  item.findViewById(R.id.likeCount)
        val postedImage:ImageView = item.findViewById(R.id.postImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val viewHolder = PostHolder(LayoutInflater.from(parent.context).inflate(R.layout.post_item,parent,false))
        viewHolder.postLikeBtn.setOnClickListener {
            listener.onLiked(snapshots.getSnapshot(viewHolder.adapterPosition).id.toString())
        }
       return viewHolder
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int, model: Post) {

        holder.postUserName.text = model.createdBy.displayName
        holder.postTitle.text  = model.post.trim()
        holder.postLikeCount.text = model.likedBy.size.toString()
        Glide.with(holder.postImage.context).load(model.createdBy.imageUrl).circleCrop().into(holder.postImage)
        holder.postCreatedAt.text = Utils.getTimeAgo(model.createdAt)

        val auth = Firebase.auth
        val currentUserId = auth.currentUser!!.uid
        val stoargeRef = FirebaseStorage.getInstance().reference
        val isLiked = model.likedBy.contains(currentUserId)
        if(isLiked) {
            holder.postLikeBtn.setImageDrawable(ContextCompat.getDrawable(holder.postLikeBtn.context, R.drawable.ic_like))
        } else {
            holder.postLikeBtn.setImageDrawable(ContextCompat.getDrawable(holder.postLikeBtn.context, R.drawable.ic_unlike))
        }

        if(model.hasImage){

//            GlobalScope.launch {
//
//                val imageUrl =stoargeRef.child("image/${model.createdAt}").downloadUrl.addOnSuccessListener {
//                    Glide.with(holder.postedImage.context).load(it).into(holder.postedImage)
//                }.await()
//
//                holder.postedImage.visibility = View.VISIBLE
//            }

            val ref = stoargeRef.child("image/${model.createdAt}")
            GlideApp.with(holder.postedImage.context).load(ref).into(holder.postedImage)
            holder.postedImage.visibility = View.VISIBLE

        }

        //decrpyt msg
//        val decryptedText = AESCrypt.decrypt(currentUserId,model.post)
//        holder.postTitle.text = decryptedText.trim()

    }

}

interface IPosAdapter{
    fun onLiked(posId:String)


}
