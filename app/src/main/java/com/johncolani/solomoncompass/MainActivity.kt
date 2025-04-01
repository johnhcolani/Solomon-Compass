package com.johncolani.solomoncompass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the video player
        setupVideoPlayer()

        // Set up the ViewPager2 for prayers
        setupViewPager()
    }

    private fun setupVideoPlayer() {
        val playerView = findViewById<PlayerView>(R.id.video_player)

        // Initialize exoPlayer
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer

        // Set the media item (video from assets)
        val mediaItem = MediaItem.fromUri("file:///android_asset/solomon_temple.mp4")
        exoPlayer.setMediaItem(mediaItem)

        // Prepare and play the video
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun setupViewPager() {
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        // List of prayers
        val prayers = listOf(
            "And now, God of Israel, let your word that you promised your servant David my father come true.\n\n1 Kings 8:26",
            "But will God really dwell on earth? The heavens, even the highest heaven, cannot contain you. How much less this temple I have built!\n\n1 Kings 8:27",
            "Yet give attention to your servant’s prayer and his plea for mercy, Lord my God. Hear the cry and the prayer that your servant is praying in your presence this day.\n\n1 Kings 8:28",
            "May your eyes be open toward this temple night and day, this place of which you said, ‘My Name shall be there,’ so that you will hear the prayer your servant prays toward this place.\n\n1 Kings 8:29"
        )

        // Set up the adapter
        val adapter = PrayerAdapter(prayers)
        viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2 for dots
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // No tab text needed, just dots
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}