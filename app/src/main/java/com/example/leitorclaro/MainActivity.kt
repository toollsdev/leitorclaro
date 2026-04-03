package com.example.leitorclaro

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.leitorclaro.ui.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    private val vm by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                BarcodeReaderApp(vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarcodeReaderApp(vm: MainViewModel) {
    val context = LocalContext.current
    val state by vm.uiState.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()
    val snackState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val cameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            scope.launch { snackState.showSnackbar("Permita acesso à câmera para capturar foto.") }
        }
    }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) pendingUri?.let(vm::processCapturedImage)
    }

    val fallbackTakePicturePreview = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        bmp?.let {
            val file = File(context.cacheDir, "preview_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { stream ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
            vm.processCapturedImage(Uri.fromFile(file))
        }
    }

    fun capturePhoto() {
        cameraPermission.launch(Manifest.permission.CAMERA)
        runCatching {
            val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            pendingUri = uri
            takePicture.launch(uri)
        }.onFailure {
            fallbackTakePicturePreview.launch(null)
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackState.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Leitor Claro") }) },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { capturePhoto() }, modifier = Modifier.fillMaxWidth()) {
                Text("Tirar foto e detectar códigos")
            }

            if (state.detectedCodes.isNotEmpty()) {
                Text("Selecione o código detectado:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.detectedCodes.forEach { code ->
                        FilterChip(
                            selected = state.selectedCode == code,
                            onClick = { vm.onSelectCode(code) },
                            label = { Text(code) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.equipmentType,
                onValueChange = { vm.onFormChange(type = it) },
                label = { Text("Tipo do equipamento") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.equipmentName,
                onValueChange = { vm.onFormChange(name = it) },
                label = { Text("Nome do equipamento") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.contract,
                onValueChange = { vm.onFormChange(contract = it) },
                label = { Text("Contrato") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = vm::saveEquipment,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar equipamento")
            }

            if (state.loading) {
                CircularProgressIndicator()
            }

            Text("Equipamentos salvos")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(items) { line ->
                    Text(line)
                }
            }
        }
    }
}
