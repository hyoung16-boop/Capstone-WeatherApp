package com.example.weatherproject.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.weatherproject.data.local.AlarmEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AlarmListScreen(
    navController: NavController,
    viewModel: AlarmViewModel = viewModel()
) {
    val alarms by viewModel.alarmList.collectAsState()
    // 전체 알림 상태 (임시 - 추후 ViewModel 또는 DataStore 연동 필요)
    var isMasterNotificationEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("알림") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("alarm_edit") }) {
                        Icon(Icons.Default.Add, contentDescription = "알림 추가")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 상단: 전체 알림 스위치 및 설명
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "전체 알림",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isMasterNotificationEnabled,
                            onCheckedChange = { isMasterNotificationEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "현재 날씨를 간략하게 알려드리는 알림 기능입니다.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // 알림 목록
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onClick = { navController.navigate("alarm_edit?alarmId=${alarm.id}") },
                        isEnabled = isMasterNotificationEnabled // 전체 알림이 꺼져있으면 비활성화된 것처럼 보이게 할 수도 있음
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmCard(alarm: AlarmEntity, onToggle: () -> Unit, onClick: () -> Unit, isEnabled: Boolean = true) {
    // 전체 알림이 꺼져있으면 투명도를 조절하여 비활성화 느낌을 줌
    val alpha = if (isEnabled) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onClick() },
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = alpha), // 배경 투명도는 적용 안될 수 있음 (Surface 색상 때문), contentColor로 조절 추천
        elevation = if (isEnabled) 4.dp else 1.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val amPm = if (alarm.hour < 12) "오전" else "오후"
                // 0시는 12시로, 13~23시는 1~11시로 표현
                val hour = if (alarm.hour == 0) 12 else if (alarm.hour > 12) alarm.hour - 12 else alarm.hour
                val minute = "%02d".format(alarm.minute)
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = amPm, fontSize = 16.sp, modifier = Modifier.padding(bottom = 6.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface.copy(alpha = alpha))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "$hour:$minute", fontSize = 32.sp, style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface.copy(alpha = alpha))
                }
                
                val infoText = if (alarm.selectedDate != null) {
                     val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                     sdf.format(Date(alarm.selectedDate))
                } else {
                    if (alarm.days.isEmpty()) "반복 없음" else alarm.days.joinToString(" ")
                }
                
                Text(text = infoText, fontSize = 14.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f * alpha))
            }
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { if (isEnabled) onToggle() }, // 전체 알림 꺼져있으면 개별 토글 불가
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                enabled = isEnabled
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AlarmEditScreen(
    navController: NavController,
    alarmId: Int = -1,
    viewModel: AlarmViewModel = viewModel()
) {
    // 12시간제 입력을 위한 상태 변수들
    var hourInput by remember { mutableStateOf("") }
    var minuteInput by remember { mutableStateOf("") }
    var isAm by remember { mutableStateOf(true) } // AM/PM 상태 (true: AM, false: PM)

    var isRepeatingMode by remember { mutableStateOf(true) }
    val daysOfWeek = listOf("월", "화", "수", "목", "금", "토", "일")
    val selectedDays = remember { mutableStateListOf<String>() }
    
    // 수정 모드일 때 로드된 알람 객체
    var currentAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    
    // 데이터 로딩 및 초기화
    LaunchedEffect(alarmId) {
        if (alarmId != -1) {
            val alarm = viewModel.getAlarmById(alarmId)
            if (alarm != null) {
                currentAlarm = alarm
                
                // 24시간제 -> 12시간제 + AM/PM 변환 로직
                if (alarm.hour == 0) {
                    hourInput = "12"
                    isAm = true
                } else if (alarm.hour == 12) {
                    hourInput = "12"
                    isAm = false
                } else if (alarm.hour > 12) {
                    hourInput = (alarm.hour - 12).toString()
                    isAm = false
                } else {
                    hourInput = alarm.hour.toString()
                    isAm = true
                }

                minuteInput = "%02d".format(alarm.minute)
                
                if (alarm.selectedDate != null) {
                    isRepeatingMode = false
                    selectedDateMillis = alarm.selectedDate
                } else {
                    isRepeatingMode = true
                    selectedDays.clear()
                    selectedDays.addAll(alarm.days)
                }
            }
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    val datePickerDialog: DatePickerDialog = remember {
        val today = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
                selectedDateMillis = calendar.timeInMillis
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = today.timeInMillis
        }
    }

    val dateText = if (selectedDateMillis != null) {
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        sdf.format(Date(selectedDateMillis!!))
    } else "날짜를 선택하세요"

    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarmId == -1) "알림 추가" else "알림 수정") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "취소")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        // 저장 로직: 12시간제 -> 24시간제 변환
                        val hInput = hourInput.toIntOrNull() ?: -1
                        val mInput = minuteInput.toIntOrNull() ?: 0 // 분 입력 없으면 0분으로 처리
                        
                        if (hInput in 1..12 && mInput in 0..59) {
                            var finalHour = hInput
                            
                            if (isAm) {
                                if (finalHour == 12) finalHour = 0 // 오전 12시는 0시
                            } else {
                                if (finalHour != 12) finalHour += 12 // 오후 1~11시는 +12 (13~23시), 오후 12시는 그대로 12시
                            }

                            if (alarmId == -1) {
                                viewModel.addAlarm(
                                    hour = finalHour,
                                    minute = mInput,
                                    days = if (isRepeatingMode) selectedDays.toList() else emptyList(),
                                    date = if (!isRepeatingMode) selectedDateMillis else null
                                )
                            } else {
                                viewModel.updateAlarmInfo(
                                    id = alarmId,
                                    hour = finalHour,
                                    minute = mInput,
                                    days = if (isRepeatingMode) selectedDays.toList() else emptyList(),
                                    date = if (!isRepeatingMode) selectedDateMillis else null
                                )
                            }
                            navController.popBackStack()
                        }
                    }) {
                        Text("저장", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("시간 설정", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // 시간 입력 Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                // AM/PM 선택 버튼
                Column(modifier = Modifier.padding(end = 16.dp)) {
                    AmPmButton(text = "오전", isSelected = isAm) { isAm = true }
                    Spacer(modifier = Modifier.height(8.dp))
                    AmPmButton(text = "오후", isSelected = !isAm) { isAm = false }
                }

                // 시(Hour) 입력
                OutlinedTextField(
                    value = hourInput,
                    onValueChange = { input ->
                        // 숫자만 허용
                        val filtered = input.filter { it.isDigit() }
                        // 2글자 제한 및 범위 검사
                        if (filtered.isEmpty()) {
                            hourInput = ""
                        } else {
                            val num = filtered.toIntOrNull() ?: 0
                            if (num > 12) {
                                hourInput = "12" // 12 초과 시 12로 고정
                            } else {
                                hourInput = filtered.take(2)
                            }
                        }
                    },
                    label = { Text("시 (1-12)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(90.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp)
                )
                
                Text(" : ", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                
                // 분(Minute) 입력
                OutlinedTextField(
                    value = minuteInput,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.isEmpty()) {
                            minuteInput = ""
                        } else {
                            val num = filtered.toIntOrNull() ?: 0
                            if (num > 59) {
                                minuteInput = "59" // 59 초과 시 59로 고정
                            } else {
                                minuteInput = filtered.take(2)
                            }
                        }
                    },
                    label = { Text("분 (0-59)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(90.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Divider(modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))

            // 반복 설정 토글
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { isRepeatingMode = true },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isRepeatingMode) MaterialTheme.colors.primary else Color.LightGray,
                        contentColor = if (isRepeatingMode) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Text("요일 반복")
                }
                Button(
                    onClick = { isRepeatingMode = false },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (!isRepeatingMode) MaterialTheme.colors.primary else Color.LightGray,
                        contentColor = if (!isRepeatingMode) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Text("날짜 지정")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isRepeatingMode) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("반복할 요일 선택", style = MaterialTheme.typography.body1)
                    
                    // 전체 선택/해제 버튼
                    TextButton(onClick = {
                        if (selectedDays.size == daysOfWeek.size) {
                            selectedDays.clear()
                        } else {
                            selectedDays.clear()
                            selectedDays.addAll(daysOfWeek)
                        }
                    }) {
                        Text(if (selectedDays.size == daysOfWeek.size) "전체 해제" else "전체 선택")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        DayToggleButton(day = day, isSelected = isSelected) {
                            if (isSelected) selectedDays.remove(day) else selectedDays.add(day)
                        }
                    }
                }
            } else {
                Text("날짜 선택", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(0.6f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant)
                ) {
                    Text(text = dateText, fontSize = 16.sp, modifier = Modifier.padding(8.dp))
                }
            }
            
            // 삭제 버튼 (수정 모드일 때만)
            if (alarmId != -1 && currentAlarm != null) {
                Spacer(modifier = Modifier.height(56.dp))
                OutlinedButton(
                    onClick = {
                        currentAlarm?.let { viewModel.deleteAlarm(it) }
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colors.error),
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    Text("이 알림 삭제")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AmPmButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) MaterialTheme.colors.primary else Color.LightGray.copy(alpha = 0.3f),
            contentColor = if (isSelected) Color.White else Color.Black.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(width = 60.dp, height = 40.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DayToggleButton(day: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colors.primary else Color.Transparent
    val contentColor = if (isSelected) Color.White else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
    val borderStroke = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor, contentColor = contentColor),
        border = borderStroke,
        shape = CircleShape,
        modifier = Modifier.size(40.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Text(text = day, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
