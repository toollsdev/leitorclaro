package com.example.leitorclaro.scanner

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await

class BarcodeScannerManager {
    suspend fun scanImage(context: Context, imageUri: Uri): List<String> {
        val image = InputImage.fromFilePath(context, imageUri)
        val scanner = BarcodeScanning.getClient()
        val barcodes = scanner.process(image).await()
        scanner.close()
        return barcodes.mapNotNull { it.rawValue }.distinct()
    }
}
