package com.dicoding.dicodingstoryapp.view.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.dicodingstoryapp.R
import com.dicoding.dicodingstoryapp.data.StoryRepository
import com.dicoding.dicodingstoryapp.databinding.ActivityMainBinding
import com.dicoding.dicodingstoryapp.view.LoadingStateAdapter
import com.dicoding.dicodingstoryapp.view.MainAdapter
import com.dicoding.dicodingstoryapp.view.MainViewModel
import com.dicoding.dicodingstoryapp.view.ViewModelFactory
import com.dicoding.dicodingstoryapp.view.add.AddStoryActivity
import com.dicoding.dicodingstoryapp.view.maps.MapsActivity
import com.dicoding.dicodingstoryapp.view.welcome.WelcomeActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: MainAdapter

    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MainAdapter()

        setupView(this)
        observeSession()
        observeView()

        binding.apply {
            fabAdd.setOnClickListener { addStory() }
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_maps -> {
                        startActivity(Intent(this@MainActivity, MapsActivity::class.java))
                        true
                    }

                    R.id.action_locale -> {
                        startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                        true
                    }

                    R.id.action_logout -> {
                        viewModel.logout()
                        StoryRepository.clearInstance()
                        ViewModelFactory.clearInstance()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }

    }

    private fun setupView(context: Context) {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()

        binding.apply {
            rvStories.layoutManager = LinearLayoutManager(context)
            rvStories.adapter = adapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    adapter.retry()
                }
            )
        }
    }

    private fun observeSession() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }

    private fun observeView() {
        viewModel.stories.observe(this, {
            adapter.submitData(lifecycle, it)
        })
    }

    private fun addStory() {
        startActivity(Intent(this, AddStoryActivity::class.java))
    }
}