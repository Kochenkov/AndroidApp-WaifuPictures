package com.vkochenkov.waifupictures.presentation.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.vkochenkov.waifupictures.R
import com.vkochenkov.waifupictures.data.BitmapStorage
import com.vkochenkov.waifupictures.data.model.PictureItem
import com.vkochenkov.waifupictures.di.App
import com.vkochenkov.waifupictures.di.App.Companion.IMAGE_ITEM
import com.vkochenkov.waifupictures.presentation.dialog.InfoBottomSheetDialog
import com.vkochenkov.waifupictures.presentation.dialog.WallpaperBottomSheetDialog
import com.vkochenkov.waifupictures.presentation.showToast
import com.vkochenkov.waifupictures.presentation.utils.ImageLoader
import com.vkochenkov.waifupictures.presentation.view_model.PictureViewModel
import com.vkochenkov.waifupictures.presentation.view_model.ViewModelFactory
import javax.inject.Inject

class PictureActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val pictureViewModel: PictureViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(PictureViewModel::class.java)
    }

    private lateinit var imageLoader: ImageLoader

    private lateinit var pictureView: SubsamplingScaleImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyDataTv: TextView

    private lateinit var wallpaperBtn: ImageView
    private lateinit var likeBtn: ImageView
    private lateinit var infoBtn: ImageView
    private lateinit var downloadBtn: ImageView
    private lateinit var shareBtn: ImageView

    private lateinit var item: PictureItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        App.appComponent.inject(this)

        initViews()
        supportActionBar?.hide()

        item = intent.getParcelableExtra(IMAGE_ITEM)!!
        val url = item.largeImageUrl

        imageLoader = ImageLoader(this, url, pictureView, emptyDataTv)
        imageLoader.setProgressBar(progressBar)
        imageLoader.loadImage()

        pictureViewModel.onCreate(item)
        initLiveDataObservers()
        setOnClickListeners()
    }

    private fun initLiveDataObservers() {
        pictureViewModel.isFavouriteImage.observe(this, Observer {
            if (it) {
                likeBtn.setImageResource(R.drawable.ic_red_favorite_24)
            } else {
                likeBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }
        })
    }

    private fun setOnClickListeners() {
        val animationWhenPressed =
            AnimationUtils.loadAnimation(this, R.anim.decreases_when_pressed)

        wallpaperBtn.setOnClickListener {
            it.startAnimation(animationWhenPressed)
            val wallpaperBottomSheetDialog = WallpaperBottomSheetDialog()
            wallpaperBottomSheetDialog.show(supportFragmentManager, "WallpaperBottomSheetDialog")
        }
        likeBtn.setOnClickListener {
            it.startAnimation(animationWhenPressed)
            pictureViewModel.onLikeButtonClick(item)
        }
        downloadBtn.setOnClickListener {
            it.startAnimation(animationWhenPressed)
            downloadImageToThePicturesGallery()
        }
        infoBtn.setOnClickListener {
            it.startAnimation(animationWhenPressed)
            val infoBottomSheetDialog = InfoBottomSheetDialog()
            if (BitmapStorage.bitmapImage!=null) {
                infoBottomSheetDialog.imageSizeInfo = "${BitmapStorage.bitmapImage!!.width}*${BitmapStorage.bitmapImage!!.height}"
            }
            infoBottomSheetDialog.show(supportFragmentManager, "InfoBottomSheetDialog")
        }
        shareBtn.setOnClickListener {
            it.startAnimation(animationWhenPressed)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, item.largeImageUrl)
            }
            startActivity(Intent.createChooser(intent, null))
        }
    }

    private fun downloadImageToThePicturesGallery() {
        if (isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            try {
                if (BitmapStorage.bitmapImage != null) {
                    MediaStore.Images.Media.insertImage(
                        contentResolver,
                        BitmapStorage.bitmapImage,
                        "Image_${System.currentTimeMillis()}", ""
                    )
                    showToast(R.string.downloading_success_str)
                } else {
                    showToast(R.string.downloading_error_str)
                }
            } catch (e: Exception) {
                showToast(R.string.downloading_error_str)
            }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissionArrays =
                arrayOf(permission)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(permissionArrays, 1)
            }
            return false
        } else {
            return true
        }
    }

    private fun initViews() {
        pictureView = findViewById(R.id.iv_full_image)
        progressBar = findViewById(R.id.progress_detail)
        emptyDataTv = findViewById(R.id.tv_empty_details)
        wallpaperBtn = findViewById(R.id.image_wallpaper_btn)
        likeBtn = findViewById(R.id.image_like_btn)
        downloadBtn = findViewById(R.id.image_download_btn)
        shareBtn = findViewById(R.id.image_share_btn)
        infoBtn = findViewById(R.id.image_info_btn)
    }
}