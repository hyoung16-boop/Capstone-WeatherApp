package com.example.weatherproject.ui

import android.app.Application
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // 검색 결과 리스트 (주소 문자열)
    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // 검색된 주소 객체들을 저장해둠 (나중에 클릭 시 좌표 꺼내쓰려고)
    private var _addressList = listOf<Address>()

    // 사용자가 입력 중인 검색어
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // 검색어가 바뀔 때 (UI 업데이트용)
    fun onSearchTextChange(text: String) {
        _searchText.value = text
        // 글자가 다 지워지면 결과창도 닫기
        if (text.isEmpty()) {
            _searchResults.value = emptyList()
            _addressList = emptyList()
        }
    }

    // ⭐️ 진짜 검색 실행 (엔터 쳤을 때 호출)
    fun performSearch() {
        val query = _searchText.value
        if (query.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) {
                    Log.e("SearchViewModel", "Geocoder not available")
                    return@launch
                }

                val geocoder = Geocoder(context, Locale.KOREA)
                // 최대 5개 결과 검색
                val addresses = geocoder.getFromLocationName(query, 5)

                if (addresses != null && addresses.isNotEmpty()) {
                    _addressList = addresses
                    // UI에 보여줄 주소 문자열 리스트 만들기
                    val addressStrings = addresses.map { address ->
                        // 전체 주소 문자열 (예: "대한민국 서울특별시 강남구 역삼동")
                        address.getAddressLine(0).replace("대한민국 ", "") // "대한민국"은 굳이 안 보여줘도 됨
                    }
                    _searchResults.value = addressStrings
                } else {
                    // 검색 결과 없음
                    _searchResults.value = listOf("검색 결과가 없습니다.")
                    _addressList = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = listOf("검색 중 오류가 발생했습니다.")
            }
        }
    }

    // 도시를 클릭했을 때 호출
    fun onCitySelected(context: Context, selectedAddressString: String, onCoordinatesFound: (Double, Double) -> Unit) {
        // 검색 실패 메시지를 클릭한 경우는 무시
        if (_addressList.isEmpty()) return

        // 클릭한 주소 문자열과 일치하는 Address 객체 찾기
        val selectedAddress = _addressList.find { 
            it.getAddressLine(0).replace("대한민국 ", "") == selectedAddressString 
        }

        if (selectedAddress != null) {
            val lat = selectedAddress.latitude
            val lon = selectedAddress.longitude
            
            Log.d("SearchViewModel", "Selected: $selectedAddressString, Lat: $lat, Lon: $lon")
            
            // 검색창 초기화
            _searchText.value = ""
            _searchResults.value = emptyList()
            _addressList = emptyList()

            // 좌표 전달
            onCoordinatesFound(lat, lon)
        } else {
            // 혹시 매칭 안 되면 다시 Geocoding 시도 (안전장치)
            viewModelScope.launch(Dispatchers.IO) {
                val geocoder = Geocoder(context, Locale.KOREA)
                val addresses = geocoder.getFromLocationName(selectedAddressString, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    withContext(Dispatchers.Main) {
                        onCoordinatesFound(addr.latitude, addr.longitude)
                        _searchText.value = ""
                        _searchResults.value = emptyList()
                    }
                }
            }
        }
    }
}