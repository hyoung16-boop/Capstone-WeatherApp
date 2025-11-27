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

@Composable
fun WeatherTopAppBar(
    viewModel: MainViewModel,
    searchViewModel: SearchViewModel,
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
            TopAppBar(
                backgroundColor = Color.Transparent,
                contentColor = Color.White,
                elevation = 0.dp,
                title = {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchViewModel.onSearchTextChange(it) },
                        label = { Text("도시 검색", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), // ⬅️ 검색 버튼 표시
                        keyboardActions = KeyboardActions(onSearch = { searchViewModel.performSearch() }), // ⬅️ 클릭 시 검색 실행
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    )
                },
                actions = {
                    // 중복 아이콘 삭제됨 (LocationOn 하나만 유지)
                    IconButton(onClick = { viewModel.refreshMyLocation() }) {
                        Icon(Icons.Default.LocationOn, "현재 위치")
                    }
                    IconButton(onClick = { navController.navigate("alarm_list") }) {
                        Icon(Icons.Default.Notifications, "알림 설정")
                    }
                }
            )

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
