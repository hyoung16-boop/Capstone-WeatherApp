package com.example.weatherproject.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.weatherproject.data.local.AlarmEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    navController: NavController,
    viewModel: AlarmViewModel = viewModel()
) {
    val alarms by viewModel.alarmList.collectAsState()

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(alarms, key = { it.id }) { alarm ->
                AlarmCard(
                    alarm = alarm,
                    onToggle = { viewModel.toggleAlarm(alarm) },
                    onClick = { navController.navigate("alarm_edit?alarmId=${alarm.id}") }
                )
            }
        }
    }
}

@Composable
fun AlarmCard(alarm: AlarmEntity, onToggle: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                val hour = if (alarm.hour == 0 || alarm.hour == 12) 12 else alarm.hour % 12
                val minute = "%02d".format(alarm.minute)
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = amPm, fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "$hour:$minute", fontSize = 32.sp, style = MaterialTheme.typography.headlineMedium)
                }
                
                val infoText = if (alarm.selectedDate != null) {
                     val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                     sdf.format(Date(alarm.selectedDate))
                } else {
                    if (alarm.days.isEmpty()) "반복 없음" else alarm.days.joinToString(" ")
                }
                
                Text(text = infoText, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    navController: NavController,
    alarmId: Int = -1,
    viewModel: AlarmViewModel = viewModel()
) {
    // 상태 변수들
    var hourInput by remember { mutableStateOf("") }
    var minuteInput by remember { mutableStateOf("") }
    var isRepeatingMode by remember { mutableStateOf(true) }
    val daysOfWeek = listOf("월", "화", "수", "목", "금", "토", "일")
    val selectedDays = remember { mutableStateListOf<String>() }
    
    // 수정 모드일 때 로드된 알람 객체
    var currentAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    
    // 데이터 로딩
    LaunchedEffect(alarmId) {
        if (alarmId != -1) {
            val alarm = viewModel.getAlarmById(alarmId)
            if (alarm != null) {
                currentAlarm = alarm
                hourInput = alarm.hour.toString()
                minuteInput = alarm.minute.toString()
                
                if (alarm.selectedDate != null) {
                    isRepeatingMode = false
                    datePickerState.selectedDateMillis = alarm.selectedDate
                } else {
                    isRepeatingMode = true
                    selectedDays.clear()
                    selectedDays.addAll(alarm.days)
                }
            }
        }
    }

    val selectedDateMillis = datePickerState.selectedDateMillis
    val dateText = if (selectedDateMillis != null) {
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        sdf.format(Date(selectedDateMillis))
    } else "날짜를 선택하세요"

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("확인") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
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
                        val h = hourInput.toIntOrNull() ?: -1
                        val m = minuteInput.toIntOrNull() ?: -1
                        
                        if (h in 0..23 && m in 0..59) {
                            if (alarmId == -1) {
                                viewModel.addAlarm(
                                    hour = h,
                                    minute = m,
                                    days = if (isRepeatingMode) selectedDays.toList() else emptyList(),
                                    date = if (!isRepeatingMode) selectedDateMillis else null
                                )
                            } else {
                                viewModel.updateAlarmInfo(
                                    id = alarmId,
                                    hour = h,
                                    minute = m,
                                    days = if (isRepeatingMode) selectedDays.toList() else emptyList(),
                                    date = if (!isRepeatingMode) selectedDateMillis else null
                                )
                            }
                            navController.popBackStack()
                        }
                    }) {
                        Text("저장")
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
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("시간 설정 (24시간제)", style = MaterialTheme.typography.labelLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                OutlinedTextField(
                    value = hourInput,
                    onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) hourInput = it },
                    label = { Text("시 (0-23)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(100.dp),
                    singleLine = true
                )
                Text(" : ", fontSize = 24.sp, modifier = Modifier.padding(horizontal = 8.dp))
                OutlinedTextField(
                    value = minuteInput,
                    onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) minuteInput = it },
                    label = { Text("분 (0-59)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(100.dp),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = isRepeatingMode,
                    onClick = { isRepeatingMode = true },
                    label = { Text("요일 반복") }
                )
                Spacer(modifier = Modifier.width(16.dp))
                FilterChip(
                    selected = !isRepeatingMode,
                    onClick = { isRepeatingMode = false },
                    label = { Text("날짜 지정") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isRepeatingMode) {
                Text("반복할 요일", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedDays.remove(day) else selectedDays.add(day)
                            },
                            label = { Text(day) },
                            modifier = Modifier.weight(1f).padding(2.dp)
                        )
                    }
                }
            } else {
                Text("날짜 선택", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showDatePicker = true }) {
                    Text(text = dateText)
                }
            }
            
            // 삭제 버튼 (수정 모드일 때만)
            if (alarmId != -1 && currentAlarm != null) {
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = {
                        currentAlarm?.let { viewModel.deleteAlarm(it) }
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("이 알림 삭제")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}