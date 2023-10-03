package com.example.imageclassification

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.imageclassification.ui.theme.BackGroundColor
import com.example.imageclassification.ui.theme.ComponentBGColor
import com.example.imageclassification.ui.theme.ImageClassificationTheme
import com.example.imageclassification.ui.theme.NavIconColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageClassificationTheme {
                // A surface container using the 'background' color from the theme
                MainScreen()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(){
    val resultStringState = remember {
        mutableStateOf("Classified as")
    }
    Scaffold(
        containerColor = BackGroundColor
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Image
            val imageModifier = Modifier
                .padding(vertical = 20.dp)
                .size(370.dp)
                .border(BorderStroke(1.dp, Color.Black))
                .background(Color.Yellow)

            val imageBitmap = remember{ mutableStateOf<ImageBitmap?>(null) }


            imageBitmap.value?.let { it1 ->
                Image(
                    bitmap = it1,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
            }

            Text(
                text = resultStringState.value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Text(
                text = "",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Buttons(imageBitmap = imageBitmap, resultState = resultStringState)

        }

    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Buttons( imageBitmap: MutableState<ImageBitmap?>, resultState: MutableState<String>){
    val context = LocalContext.current
    val cameraPermissionState =
        rememberPermissionState(permission = android.Manifest.permission.CAMERA)

    val lifecycleOwner = LocalLifecycleOwner.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()){isGranted ->
    }
    
    val resultLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult() ){result->

    }

    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview() ){  bitmap ->
        if (bitmap != null) {
            imageBitmap.value = bitmap.asImageBitmap()

            classifyImage(bitmap, context = context, resultStringState = resultState)
        }
    }

    FilledTonalButton(
        onClick = {
            val permissionResult = cameraPermissionState.status

            if (!permissionResult.isGranted) {
                if (permissionResult.shouldShowRationale) {
                    // Show a rationale if needed (optional)
                }
                else {
                    // Request the permission

                    lifecycleOwner.lifecycleScope.launch {
                        val result = requestPermissionLauncher.launch(cameraPermissionState.permission)
                    }

                }
            }
            else{
               val intent =  Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                resultLauncher.launch(intent)
                
            }


        },
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = ComponentBGColor,
            contentColor = NavIconColor
        ),
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        Text(text = "Take Picture")

    }

    FilledTonalButton(
        onClick = {
            val permissionResult = cameraPermissionState.status

            if (!permissionResult.isGranted) {
                if (permissionResult.shouldShowRationale) {
                    // Show a rationale if needed (optional)
                }
                else {
                    // Request the permission
                    lifecycleOwner.lifecycleScope.launch {
                        val result = requestPermissionLauncher.launch(cameraPermissionState.permission)
                    }

                }
            }
            else{
                val intent =  Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch()
            }

        },
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = ComponentBGColor,
            contentColor = NavIconColor
        ),
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        Text(text = "Launch Camera")
    }
}
