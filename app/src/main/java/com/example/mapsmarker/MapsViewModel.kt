package com.example.mapsmarker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsViewModel: ViewModel() {

    private var _map = MutableStateFlow<GoogleMap?>(null)
    val map: StateFlow<GoogleMap?> = _map.asStateFlow()
//    private var __map: GoogleMap? = null

    fun getMap(){
        viewModelScope.launch {
            _map.emit()
        }

    }

    fun getDrivers() {
        viewModelScope.launch {
            _drivers.emit(withContext(Dispatchers.IO) { getAllDriversListUseCase.execute() })
        }
    }

    fun deleteDriver(driverId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteDriverUseCase.execute(driverId)
        }
    }


}