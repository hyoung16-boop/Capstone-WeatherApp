package com.example.weatherproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// 가상의 CCTV 데이터 모델
data class CctvLocation(val id: Int, val name: String, val status: String = "정상 작동 중")

@Composable
fun CctvScreen(navController: NavController) {
    // 가상의 CCTV 데이터 목록 (전국 주요 지점)
    val allCctvs = remember {
        listOf(
            CctvLocation(1, "서울 강남대로 강남역"),
            CctvLocation(2, "서울 올림픽대로 한남대교"),
            CctvLocation(3, "서울 강변북로 반포대교"),
            CctvLocation(4, "서울 서초구 반포동"),
            CctvLocation(5, "서울 송파구 잠실역"),
            CctvLocation(6, "서울 마포구 합정동"),
            CctvLocation(7, "경기 성남시 판교 IC"),
            CctvLocation(8, "경기 수원시 정자 사거리"),
            CctvLocation(9, "부산 해운대구 센텀시티"),
            CctvLocation(10, "부산 광안대교 상판"),
            CctvLocation(11, "대구 수성구 범어네거리"),
            CctvLocation(12, "인천 부평구 부평역"),
            CctvLocation(13, "대전 중구 유천동"),
            CctvLocation(14, "광주 서구 상무지구"),
            CctvLocation(15, "제주 동문시장 입구")
        )
    }

    var searchText by remember { mutableStateOf("") }

    // 검색어에 따라 필터링된 리스트
    val filteredCctvs = if (searchText.isEmpty()) {
        allCctvs
    } else {
        allCctvs.filter { it.name.contains(searchText, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("실시간 교통 CCTV", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기", tint = Color.White)
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            )
        },
        backgroundColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // 검색창
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("지역 또는 동네 검색 (예: 강남, 판교)", color = Color.White.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "삭제", tint = Color.White)
                        }
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    backgroundColor = Color.White.copy(alpha = 0.1f)
                ),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // CCTV 리스트
            if (filteredCctvs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("검색 결과가 없습니다.", color = Color.White.copy(alpha = 0.7f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredCctvs) { cctv ->
                        CctvItemCard(cctv = cctv)
                    }
                }
            }
        }
    }
}

@Composable
fun CctvItemCard(cctv: CctvLocation) {
    Card(
        backgroundColor = Color.Black.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 실제 CCTV 영상이 들어갈 자리 (플레이스홀더)
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "CCTV 화면 로딩 중...", color = Color.Gray)
            }

            // 하단 정보 바
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
            ) {
                Text(
                    text = cctv.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = cctv.status,
                    color = Color.Green,
                    fontSize = 12.sp
                )
            }
            
            // 라이브 배지
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.Red, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}