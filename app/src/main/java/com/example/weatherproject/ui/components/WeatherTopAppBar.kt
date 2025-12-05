package com.example.weatherproject.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.weatherproject.ui.MainViewModel
import com.example.weatherproject.ui.SearchViewModel

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

import androidx.compose.ui.text.style.TextOverflow
import com.example.weatherproject.ui.CctvViewModel

@Composable
fun WeatherTopAppBar(
    viewModel: MainViewModel,
    searchViewModel: SearchViewModel,
    cctvViewModel: CctvViewModel,
    navController: NavController
) {
    val searchText by searchViewModel.searchText.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
    ) {
        Column {
            // TopAppBar 대신 커스텀 Row 사용 (높이 제한 해결)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp) // 넉넉한 높이 확보
                    .padding(horizontal = 16.dp, vertical = 8.dp), // 상하좌우 여백
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 검색 입력창 (가중치 부여로 남은 공간 차지)
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchViewModel.onSearchTextChange(it) },
                    label = { Text("도시 검색", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { searchViewModel.performSearch() }),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .weight(1f) // 아이콘 공간 제외하고 꽉 채우기
                        .padding(end = 8.dp)
                )

                // 아이콘 버튼들 (Row로 묶음)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.refreshMyLocation() }) {
                        Icon(Icons.Default.LocationOn, "현재 위치", tint = Color.White)
                    }
                    IconButton(onClick = { navController.navigate("alarm_list") }) {
                        Icon(Icons.Default.Notifications, "알림 설정", tint = Color.White)
                    }
                }
            }

            if (searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                ) {
                    items(searchResults) { city ->
                        Text(
                            text = city,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchViewModel.onCitySelected(context, city) { lat, lon ->
                                        viewModel.updateWeatherByLocation(city, lat, lon)
                                        cctvViewModel.updateSelectedLocation(lat, lon, city, null)
                                    }
                                }
                                .padding(16.dp),
                            color = Color.Black,
                            maxLines = 1, // ⬅️ 한 줄 제한
                            overflow = TextOverflow.Ellipsis // ⬅️ 말줄임표(...) 처리
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
