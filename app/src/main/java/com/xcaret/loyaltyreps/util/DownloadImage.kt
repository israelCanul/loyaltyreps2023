package com.xcaret.loyaltyreps.util

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.koushikdutta.ion.Ion
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class DownloadImage {
    public fun save(context: Context, activity: Activity, url: String, Name: String){
        var cont: Boolean = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionCheck: Int = ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val permissionCheck2: Int = ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            when {
                //permissionCheck != PackageManager.PERMISSION_GRANTED -> ActivityCompat.requestPermissions(activity, arrayOf( Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                permissionCheck2 != PackageManager.PERMISSION_GRANTED -> {
                    cont = false;
                    ActivityCompat.requestPermissions(activity, arrayOf( Manifest.permission.WRITE_EXTERNAL_STORAGE), 123)
                }
            }
        }
        kotlin.run {
            if(cont) {
                var request = DownloadManager.Request(Uri.parse(url))
                    .setTitle(Name)
                    .setDescription(Name + " Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setAllowedOverMetered(true)
                    //.setDestinationInExternalPublicDir(context?.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() ,arguments?.getString("xpark_name").toString() + ".mp4")
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        Name + ".jpg"
                    )
                var dm: DownloadManager =
                    context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(context, "Downloading ${Name}", Toast.LENGTH_LONG).show()
            }
        }
    }

    public fun saveVideo(context: Context, activity: Activity, xvideoUrl: String, Name: String):Long{
        var cont: Boolean = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionCheck: Int = ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

            val permissionCheck2: Int = ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            when {
               permissionCheck2 != PackageManager.PERMISSION_GRANTED -> {
                    cont = false;
                    ActivityCompat.requestPermissions(activity, arrayOf( Manifest.permission.WRITE_EXTERNAL_STORAGE), 123)
                }
            }
        }
        kotlin.run {
            if(cont) {
                var request = DownloadManager.Request(Uri.parse(xvideoUrl))
                    .setTitle(Name)
                    .setDescription(Name + "Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setAllowedOverMetered(true)
                    //.setDestinationInExternalPublicDir(context?.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() ,arguments?.getString("xpark_name").toString() + ".mp4")
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        Name + ".mp4"
                    )
                var dm: DownloadManager =
                    context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                Toast.makeText(context, "Downloading ${Name}", Toast.LENGTH_LONG).show()
                return dm.enqueue(request)
            }else{
                return 0;
            }
        }
    }


}