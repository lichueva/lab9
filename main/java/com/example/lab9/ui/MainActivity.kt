package com.example.lab9.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab9.databinding.ActivityMainBinding
import com.example.lab9.models.Post
import com.example.lab9.network.RetrofitClient
import com.example.lab9.utils.NetworkUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (NetworkUtil.isNetworkAvailable(this)) {
            fetchPosts()
        } else {
            showNoInternetMessage()
        }
    }

    private fun fetchPosts() {

        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getPosts()

                if (response.isSuccessful) {
                    val posts = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {

                        binding.progressBar.visibility = View.GONE

                        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                        binding.recyclerView.adapter = PostAdapter(posts) { post ->

                            val intent = Intent(this@MainActivity, PostDetailsActivity::class.java)
                            intent.putExtra("POST_ID", post.id)
                            startActivity(intent)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.adapter = PostAdapter(emptyList()) { }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.adapter = PostAdapter(emptyList()) { }
                    Toast.makeText(this@MainActivity, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showNoInternetMessage() {
        Toast.makeText(this, "Немає інтернет з'єднання", Toast.LENGTH_SHORT).show()
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.adapter = PostAdapter(emptyList()) { }
    }
}
