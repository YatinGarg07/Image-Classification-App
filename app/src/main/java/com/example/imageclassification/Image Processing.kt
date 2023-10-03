package com.example.imageclassification

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.example.imageclassification.ml.Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder


fun classifyImage(bitmap: Bitmap,context: Context, resultStringState: MutableState<String>?) {
    val newImage = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize,false)
    val model = Model.newInstance(context)

// Creates inputs for reference.
    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 32, 32, 3), DataType.FLOAT32)
    val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
    byteBuffer.order(ByteOrder.nativeOrder())

    val intValues = IntArray(imageSize * imageSize)
    newImage.getPixels(intValues, 0, newImage.width, 0, 0 , newImage.width, newImage.height)
    var pixel =0;
    for(i in 0 until imageSize){
        for(j in 0 until imageSize){
            val values = intValues[pixel++] // RGB
            byteBuffer.putFloat(( (values shr(16)) and(0xFF) ) * (1f / 1))
            byteBuffer.putFloat(( (values shr(8)) and(0xFF) ) * (1f / 1))
            byteBuffer.putFloat(( values and(0xFF) ) * (1f / 1))

        }

    }

    inputFeature0.loadBuffer(byteBuffer)

    val outputs = model.process(inputFeature0)
    val outputFeature0 = outputs.outputFeature0AsTensorBuffer

    val confidences = outputFeature0.floatArray
    // find the index of the class with the biggest confidence
    var maxPos = 0
    var maxConfidence = 0f;
    for (i in 0 until confidences.size){
        println(confidences[i])
        if(confidences[i] > maxConfidence){
            maxConfidence = confidences[i]
            maxPos = i;
        }
    }



    val classes = listOf<String>("Apple", "Banana", "Orange");
    resultStringState?.value = classes[maxPos]

// Releases model resources if no longer used.
    model.close()
}
