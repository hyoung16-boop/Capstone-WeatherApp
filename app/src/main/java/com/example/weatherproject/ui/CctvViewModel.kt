package com.example.weatherproject.ui

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherproject.data.CctvInfo
import com.example.weatherproject.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val currentLocationForDistance: Location?
)

/**
 * CCTV 화면의 상태와 비즈니스 로직을 관리하는 ViewModel입니다.
 *
 * 주요 역할:
 * 1. 선택된 위치 주변의 CCTV 정보를 조회합니다.
 * 2. 현재 위치와 CCTV 사이의 거리를 계산합니다.
 * 3. CCTV 정보 로딩 상태 및 에러 상태를 관리합니다.
 */
@HiltViewModel
class CctvViewModel @Inject constructor(
    private val repository: WeatherRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _cctvInfo = MutableStateFlow<CctvInfo?>(null)
    val cctvInfo: StateFlow<CctvInfo?> = _cctvInfo

    private val _cctvError = MutableStateFlow<String?>(null)
    val cctvError: StateFlow<String?> = _cctvError

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedLocationInfo = MutableStateFlow<LocationInfo?>(null)
    val selectedLocationInfo: StateFlow<LocationInfo?> = _selectedLocationInfo.asStateFlow()

    /**
     * CCTV를 조회할 기준 위치를 업데이트하고, 해당 위치의 CCTV 정보를 서버에 요청합니다.
     *
     * @param lat 위도
     * @param lon 경도
     * @param address 주소 텍스트
     * @param currentLocation 현재 사용자 위치 (거리 계산용)
     * @param forceRefresh 거리 제한 무시하고 강제 갱신 여부
     */
    fun updateSelectedLocation(
        lat: Double, 
        lon: Double, 
        address: String, 
        currentLocation: Location?, 
        forceRefresh: Boolean = false
    ) {
        val lastInfo = _selectedLocationInfo.value
        
        // 중복 호출 방지 및 거리 필터링:
        // forceRefresh가 false일 때만 거리 체크를 수행합니다.
        if (!forceRefresh && lastInfo != null) {
            val dist = calculateDistance(lastInfo.latitude, lastInfo.longitude, lat, lon)
            // 500m(0.5km) 미만이면 갱신 안 함
            if (dist < 0.5) {
                return
            }
        }

        val newLocationInfo = LocationInfo(lat, lon, address, currentLocation)
        
        _selectedLocationInfo.value = newLocationInfo
        fetchCctvByLocation()
    }
    
    fun retryFetchCctv() {
        fetchCctvByLocation()
    }

    private fun fetchCctvByLocation() {
        val locationInfo = _selectedLocationInfo.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _cctvInfo.value = null
            _cctvError.value = null

            try {
                Log.d("CctvViewModel", "CCTV 검색: Lat=${locationInfo.latitude}, Lng=${locationInfo.longitude}")

                val result = repository.getNearbyCctv(lat = locationInfo.latitude, lng = locationInfo.longitude)

                if (result.isSuccess) {
                    val response = result.getOrThrow()
                    
                    if (response.status == "success") {
                        val distance = locationInfo.currentLocationForDistance?.let { currentLoc ->
                            calculateDistance(
                                currentLoc.latitude,
                                currentLoc.longitude,
                                response.cctvLat.toDoubleOrNull() ?: 0.0,
                                response.cctvLng.toDoubleOrNull() ?: 0.0
                            )
                        }

                        val cctvData = CctvInfo(
                            cctvName = response.cctvName,
                            cctvUrl = response.cctvUrl,
                            type = response.cctvType,
                            roadName = response.cctvName.split(" ").firstOrNull() ?: "",
                            distance = distance?.let { String.format("%.1fkm", it) } ?: "",
                            latitude = response.cctvLat,
                            longitude = response.cctvLng
                        )
                        _cctvInfo.value = cctvData
                        Log.d("CctvViewModel", "CCTV 정보 업데이트 완료: ${cctvData.cctvName}")
                    } else {
                        _cctvError.value = "주변에 CCTV 정보가 없습니다."
                    }
                } else {
                     _cctvError.value = "CCTV 정보를 가져오는 데 실패했습니다."
                     Log.e("CctvViewModel", "CCTV API 호출 실패", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e("CctvViewModel", "CCTV API 호출 실패: ${e.message}", e)
                _cctvError.value = "CCTV 정보를 가져오는 데 실패했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}