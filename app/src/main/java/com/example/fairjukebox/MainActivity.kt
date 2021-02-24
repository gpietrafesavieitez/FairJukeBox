package com.example.fairjukebox

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val CLIENT_ID = resources.getString(R.string.client_id)
    private val REDIRECT_URI = resources.getString(R.string.redirect_uri)
    private val RANDOM = resources.getString(R.string.random)
    private val FREE = resources.getString(R.string.free)
    private val BEAST = resources.getString(R.string.beast)
    private val REPS = resources.getString(R.string.reps)
    private val TECH = resources.getString(R.string.tech)
    private val DANCE = resources.getString(R.string.dance)

    private var mSpotifyAppRemote: SpotifyAppRemote? = null
    private var uri: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()
        SpotifyAppRemote.connect(this, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.d("debug", "Connected!")
                    mSpotifyAppRemote!!.playerApi
                        .subscribeToPlayerState()
                        .setEventCallback { playerState: PlayerState ->
                            val track: Track? = playerState.track
                            if (track != null) {
                                updateTrackCoverArt(playerState)
                                txtAlbum.text = track.name + " - " + track.artist.name
                                Log.d("debug", track.toString())
                            }
                            btnRandom.setOnClickListener {
                                uri = RANDOM
                                updateColors()
                            }
                            btnFree.setOnClickListener {
                                uri = FREE
                                updateColors()
                            }
                            btnBeast.setOnClickListener {
                                uri = BEAST
                                updateColors()
                            }
                            btnReps.setOnClickListener {
                                uri = REPS
                                updateColors()
                            }
                            btnTech.setOnClickListener {
                                uri = TECH
                                updateColors()
                            }
                            btnDance.setOnClickListener {
                                uri = DANCE
                                updateColors()
                            }
                            btnPlay.setOnClickListener {
                                if(playerState.isPaused){
                                    mSpotifyAppRemote!!.playerApi.resume()
                                    btnPlay.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause))
                                }else{
                                    mSpotifyAppRemote!!.playerApi.pause()
                                    btnPlay.setImageDrawable(getDrawable(android.R.drawable.ic_media_play))
                                }
                            }
                            btnNext.setOnClickListener {
                                spotify()
                            }
                            btnPrev.setOnClickListener {
                                mSpotifyAppRemote!!.playerApi.skipPrevious()
                            }
                        }
                }
                override fun onFailure(throwable: Throwable) {
                    Log.e("debug", throwable.message, throwable)
                }
            })
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

    private fun updateColors(){
        btnFree.setColorFilter(ContextCompat.getColor(this, R.color.colorOff))
        btnBeast.setColorFilter(ContextCompat.getColor(this, R.color.colorOff))
        btnReps.setColorFilter(ContextCompat.getColor(this, R.color.colorOff))
        btnRandom.setColorFilter(ContextCompat.getColor(this, R.color.colorOff))
        btnTech.setColorFilter(ContextCompat.getColor(this, R.color.colorOff))
        btnDance.setColorFilter(ContextCompat.getColor(this, R.color.colorOff))
        if(uri == FREE){
            btnFree.setColorFilter(ContextCompat.getColor(this, R.color.colorFree))
        }else if(uri == BEAST){
            btnBeast.setColorFilter(ContextCompat.getColor(this, R.color.colorBeast))
        }else if(uri == REPS){
            btnReps.setColorFilter(ContextCompat.getColor(this, R.color.colorReps))
        }else if(uri == RANDOM){
            btnRandom.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite))
        }else if(uri == TECH){
            btnTech.setColorFilter(ContextCompat.getColor(this, R.color.colorTech))
        }else if(uri == DANCE){
            btnDance.setColorFilter(ContextCompat.getColor(this, R.color.colorDance))
        }
    }

    private fun spotify(){
        if(uri.isBlank() || uri.isEmpty()){
            mSpotifyAppRemote!!.playerApi.skipNext()
        }else{
            mSpotifyAppRemote!!.playerApi.play(uri)
            mSpotifyAppRemote!!.playerApi
                .subscribeToPlayerState()
                .setEventCallback { playerState: PlayerState ->
                    val track: Track? = playerState.track
                    if (track != null) {
                        if(!playerState.isPaused){
                            btnPlay.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause))
                        }else{
                            btnPlay.setImageDrawable(getDrawable(android.R.drawable.ic_media_play))
                        }
                        updateTrackCoverArt(playerState)
                        txtAlbum.text = track.name + " - " + track.artist.name
                        Log.d("debug", track.toString())
                        if(!playerState.playbackOptions.isShuffling){
                            Log.d("debug", "is shuffling")
                            mSpotifyAppRemote!!.playerApi.toggleShuffle()
                        }
                    }
                }
        }
    }

    private fun assertAppRemoteConnected(): SpotifyAppRemote {
        mSpotifyAppRemote?.let {
            if (it.isConnected) {
                return it
            }
        }
        throw SpotifyDisconnectedException()
    }

    private fun updateTrackCoverArt(playerState: PlayerState) {
        assertAppRemoteConnected()
            .imagesApi
            .getImage(playerState.track.imageUri, Image.Dimension.MEDIUM)
            .setResultCallback { bitmap ->
                imgAlbum.setImageBitmap(bitmap)
            }
    }

}