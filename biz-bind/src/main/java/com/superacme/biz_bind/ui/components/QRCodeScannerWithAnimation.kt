package com.superacme.biz_bind.ui.components

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRCodeScannerWithAnimation(
    onBarcodeScanned: (String) -> Unit
) {
//    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanLineOffset = remember { Animatable(0f) }

    var isStopScanLineAnimation by remember { mutableStateOf(false) }

    // Start the animation for the scanning line
    LaunchedEffect(Unit) {
        while (!isStopScanLineAnimation) {
            scanLineOffset.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
            scanLineOffset.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    // 设置扫描条码的格式，Google ML Kit 支持多种条码格式
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            // 只扫描 QR 码
                            Barcode.FORMAT_QR_CODE,
                        )
                        // 允许返回所有可能的条形码
                        .enableAllPotentialBarcodes()
                        .build()

                    val barcodeScanner = BarcodeScanning.getClient(options)
                    val analysisUseCase = ImageAnalysis.Builder()
                        .setTargetResolution(android.util.Size(1280, 720)) // 设置分辨率
                        // 限制扫描帧率，只处理最新帧
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        // 扫描帧
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->

                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val inputImage = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    barcodeScanner.process(inputImage)
                                        .addOnSuccessListener { barcodes ->
                                            barcodes.firstOrNull()?.rawValue?.let { barcodeValue ->
                                                onBarcodeScanned(barcodeValue)
                                                // 扫描有结果就停止扫描了
                                                if (barcodeValue.isNotEmpty()) {
                                                    isStopScanLineAnimation = true
                                                    cameraProvider.unbindAll() // 停止扫描
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                "QRCodeScanner",
                                                "Barcode detection failed: ${e.message}"
                                            )
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                }
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysisUseCase
                        )
                    } catch (exc: Exception) {
                        exc.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 绘制半透明蒙层和扫描框
        Canvas(modifier = Modifier.fillMaxSize()) {
            val overlayColor = Color.Black.copy(alpha = 0.5f)
            val frameWidth = 250.dp.toPx()
            val frameHeight = 250.dp.toPx()
            val cornerLength = 20.dp.toPx()
            val strokeWidth = 4.dp.toPx()
            val centerX = size.width / 2
            val centerY = size.height / 2
            val left = centerX - frameWidth / 2
            val top = centerY - frameHeight / 2
            val right = centerX + frameWidth / 2
            val bottom = centerY + frameHeight / 2

            // 绘制四周的半透明蒙层
            drawRect(color = overlayColor, size = size)
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(frameWidth, frameHeight),
                blendMode = BlendMode.Clear
            )

            // 绘制扫描框四角
            val greenColor = Color.LightGray

            // 左上角
            drawLine(
                color = greenColor,
                start = Offset(left, top),
                end = Offset(left + cornerLength, top),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = greenColor,
                start = Offset(left, top),
                end = Offset(left, top + cornerLength),
                strokeWidth = strokeWidth
            )

            // 右上角
            drawLine(
                color = greenColor,
                start = Offset(right, top),
                end = Offset(right - cornerLength, top),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = greenColor,
                start = Offset(right, top),
                end = Offset(right, top + cornerLength),
                strokeWidth = strokeWidth
            )

            // 左下角
            drawLine(
                color = greenColor,
                start = Offset(left, bottom),
                end = Offset(left + cornerLength, bottom),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = greenColor,
                start = Offset(left, bottom),
                end = Offset(left, bottom - cornerLength),
                strokeWidth = strokeWidth
            )

            // 右下角
            drawLine(
                color = greenColor,
                start = Offset(right, bottom),
                end = Offset(right - cornerLength, bottom),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = greenColor,
                start = Offset(right, bottom),
                end = Offset(right, bottom - cornerLength),
                strokeWidth = strokeWidth
            )
        }

//        // 扫描框的边框
//        Box(
//            modifier = Modifier
//                .size(250.dp)
//                .border(2.dp, Color.LightGray, RectangleShape)
//        )

        // 渐变扫描线动画
        Canvas(
            modifier = Modifier
                .size(250.dp)
                .graphicsLayer {
                    translationY = scanLineOffset.value * size.height
                }
        ) {
            val gradientBrush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0f),
                    Color.White,
                    Color.White.copy(alpha = 0f)
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f)
            )
            drawLine(
                brush = gradientBrush,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 4.dp.toPx()
            )
        }
    }
}
