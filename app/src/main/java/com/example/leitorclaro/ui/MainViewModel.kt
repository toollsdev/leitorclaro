package com.example.leitorclaro.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.leitorclaro.data.AppDatabase
import com.example.leitorclaro.data.Equipment
import com.example.leitorclaro.data.EquipmentRepository
import com.example.leitorclaro.location.LocationProvider
import com.example.leitorclaro.scanner.BarcodeScannerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val loading: Boolean = false,
    val detectedCodes: List<String> = emptyList(),
    val selectedCode: String? = null,
    val equipmentType: String = "",
    val equipmentName: String = "",
    val contract: String = "",
    val message: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = EquipmentRepository(AppDatabase.get(application).equipmentDao())
    private val scanner = BarcodeScannerManager()
    private val locationProvider = LocationProvider(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val items = repo.observeAll().map { list ->
        list.map {
            "${it.equipmentName} | ${it.barcode} | ${it.equipmentType} | ${it.contract}"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onFormChange(type: String? = null, name: String? = null, contract: String? = null) {
        _uiState.update {
            it.copy(
                equipmentType = type ?: it.equipmentType,
                equipmentName = name ?: it.equipmentName,
                contract = contract ?: it.contract
            )
        }
    }

    fun onSelectCode(code: String) {
        _uiState.update { it.copy(selectedCode = code) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun processCapturedImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, message = null) }
            runCatching { scanner.scanImage(getApplication(), uri) }
                .onSuccess { codes ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            detectedCodes = codes,
                            selectedCode = codes.firstOrNull(),
                            message = if (codes.isEmpty()) "Nenhum código detectado." else null
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(loading = false, message = "Falha ao ler código: ${it.message}")
                    }
                }
        }
    }

    fun saveEquipment() {
        val current = _uiState.value
        if (current.selectedCode.isNullOrBlank()) {
            _uiState.update { it.copy(message = "Selecione um código para salvar.") }
            return
        }
        if (current.equipmentName.isBlank() || current.equipmentType.isBlank()) {
            _uiState.update { it.copy(message = "Preencha tipo e nome do equipamento.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            runCatching {
                val address = locationProvider.fetchAddressInfo()
                repo.insert(
                    Equipment(
                        barcode = current.selectedCode,
                        equipmentType = current.equipmentType,
                        equipmentName = current.equipmentName,
                        contract = current.contract,
                        capturedAt = System.currentTimeMillis(),
                        latitude = address.latitude,
                        longitude = address.longitude,
                        street = address.street,
                        neighborhood = address.neighborhood,
                        postalCode = address.postalCode
                    )
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        loading = false,
                        detectedCodes = emptyList(),
                        selectedCode = null,
                        equipmentType = "",
                        equipmentName = "",
                        contract = "",
                        message = "Equipamento salvo com sucesso."
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(loading = false, message = "Erro ao salvar: ${it.message}") }
            }
        }
    }
}
