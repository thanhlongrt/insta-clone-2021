package com.example.instagram.ui.create

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.instagram.utils.Constants.KEY_IS_VIDEO
import com.example.instagram.utils.Constants.KEY_URI
import com.example.instagram.R
import com.example.instagram.databinding.FragmentPreviewVideoBinding
import com.example.instagram.extensions.getFragmentNavController
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import com.otaliastudios.transcoder.source.UriDataSource
import com.otaliastudios.transcoder.validator.WriteVideoValidator
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by Thanh Long Nguyen on 5/9/2021
 */

class PreviewVideoFragment : Fragment() {
    companion object {
        private const val TAG = "PreviewVideoFragment"
    }

    private var binding: FragmentPreviewVideoBinding? = null

    private var videoUri: Uri? = null

    private var player: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var currentVolume: Float = 0f
    private var containAudio: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            videoUri = it.getParcelable(KEY_URI)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPreviewVideoBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val checkBox = binding?.playerView?.findViewById<CheckBox>(R.id.exo_toggle_sound)!!

        binding?.toolBar?.inflateMenu(R.menu.menu_next)
        binding?.toolBar?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_next -> {
                    val bundle = bundleOf(
                        KEY_URI to videoUri,
                        KEY_IS_VIDEO to true
                    )
                    findNavController().navigate(
                        R.id.action_previewVideoFragment_to_createNewPostFragment,
                        bundle
                    )
                    true
                }

                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }

        binding?.backButton?.setOnClickListener {
            getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
        }

        binding?.playerView?.setOnClickListener {
            checkBox.isChecked = !checkBox.isChecked
            toggleSound()
        }
    }


    private fun toggleSound() {
//        if (containAudio){
        if (player?.volume != 0f) {
            currentVolume = player?.volume!!
            player?.volume = 0f
        } else {
            player?.volume = currentVolume
        }
//        }
    }

    private fun initializePlayer(uri: Uri, context: Context) {
        binding?.playerView?.let {
            val trackSelector = DefaultTrackSelector(context)
            trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd()
            )
            player = SimpleExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .build()
        }
        binding?.playerView?.player = player
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .build()

        player?.apply {
            setMediaItem(mediaItem)
            repeatMode = SimpleExoPlayer.REPEAT_MODE_ONE
            playWhenReady = this@PreviewVideoFragment.playWhenReady
            seekTo(currentWindow, playbackPosition)
//            addListener(playbackStateListener)
            prepare()
        }
    }

    private fun releasePlayer() {
        player?.let { player ->
            playbackPosition = player.currentPosition
            currentWindow = player.currentWindowIndex
            playWhenReady = player.playWhenReady
//            player.removeListener(playbackStateListener)
            player.release()
            this.player = null
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= 24 && videoUri != null) {
            Log.e(TAG, "onStart: $videoUri")
            initializePlayer(videoUri!!, requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < 24 && videoUri != null && player == null) {
            initializePlayer(videoUri!!, requireContext())
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < 24 && videoUri != null) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= 24 && videoUri != null) {
            releasePlayer()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    fun transcode(uri: Uri) {
        val transcodeOutputFile: File
        try {
            val outputDir = File(requireContext().getExternalFilesDir(null), "Videos")
            outputDir.mkdir()
            transcodeOutputFile = File.createTempFile("Transcode Test", "mp4", outputDir)
            Log.e(TAG, "transcoding into: $transcodeOutputFile")
        } catch (e: IOException) {
            Log.e(TAG, "transcode: failed to  create temp file, $e")
            return
        }

        val uriSource = UriDataSource(requireContext(), uri)

        Transcoder.into(transcodeOutputFile.absolutePath)
            .addDataSource(uriSource)
            .setValidator(WriteVideoValidator())
            .setListener(object : TranscoderListener {
                override fun onTranscodeProgress(progress: Double) {
                    Log.e(TAG, "onTranscodeProgress: $progress")
                }

                override fun onTranscodeCompleted(successCode: Int) {
                    val resultUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.instagram",
                        transcodeOutputFile
                    )
                    Log.e(TAG, "onTranscodeCompleted: result: $resultUri")
                }

                override fun onTranscodeCanceled() {
                }

                override fun onTranscodeFailed(exception: Throwable) {
                }
            })
            .transcode()
    }
}