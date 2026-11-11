package com.kormi.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoCallScreen(navController: NavController, roomName: String, isVideoCall: Boolean) {
    // Jitsi Meet-এর রুম লিংক
    val url = "https://meet.jit.si/$roomName"

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // পারমিশন রেজাল্ট হ্যান্ডেল করা (WebView নিজেই হ্যান্ডেল করবে)
    }

    LaunchedEffect(Unit) {
        // কল শুরু হওয়ার আগে অডিও এবং ক্যামেরা পারমিশন চেক করা
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (isVideoCall) permissions.add(Manifest.permission.CAMERA)

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(navController.context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false // অটো প্লে-র জন্য
                    
                    // ওয়েবসাইটকে মাইক এবং ক্যামেরা ব্যবহারের অনুমতি দেওয়া
                    webChromeClient = object : WebChromeClient() {
                        override fun onPermissionRequest(request: PermissionRequest) {
                            request.grant(request.resources)
                        }
                    }
                    
                    if (!isVideoCall) {
                        // যদি শুধু অডিও কল হয়, তবে URL-এ ভিডিও বন্ধ করার প্যারামিটার যুক্ত করা (Jitsi Config)
                        loadUrl("$url#config.startWithVideoMuted=true")
                    } else {
                        loadUrl(url)
                    }
                }
            }
        )
    }
}
