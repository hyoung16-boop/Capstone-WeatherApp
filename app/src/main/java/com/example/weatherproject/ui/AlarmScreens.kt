package com.example.weatherproject.ui

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weatherproject.data.local.AlarmEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 공통 배경 (파란색 그라데이션)
val BlueGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF60A5FA),
        Color(0xFF93C5FD)
    )
)

@Composable
fun AlarmListScreen(
    navController: NavController,
    viewModel: AlarmViewModel = hiltViewModel()
) {
    val alarms by viewModel.alarmList.collectAsState()
    val settings by viewModel.settings.collectAsState()


    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    var hasExactAlarmPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        )
    }
    
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasExactAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
            if (!isGranted) {
                Toast.makeText(context, "알림 권한이 거부되어 알림을 추가할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    var showPermissionDialog by remember { mutableStateOf(false) }

    val onAddAlarmClick = {
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else { true }

        hasExactAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else { true }

        if (hasNotificationPermission && hasExactAlarmPermission) {
            navController.navigate("alarm_edit")
        } else {
            showPermissionDialog = true
        }
    }

    if (showPermissionDialog) {
        PermissionRequestDialog(
            hasNotificationPermission = hasNotificationPermission,
            hasExactAlarmPermission = hasExactAlarmPermission,
            onDismiss = { showPermissionDialog = false },
            onConfirmNotification = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onConfirmExactAlarm = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                    }
                    settingsLauncher.launch(intent)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueGradient)
    ) {
        Scaffold(
            backgroundColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("알림", color = Color.White) },
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기", tint = Color.White)
                        }
                    },
                    actions = {
                        // 사용자 지정 알림이 활성화 되어있을 때만 추가 버튼 표시
                        AnimatedVisibility(visible = settings.isMasterEnabled && settings.isUserAlarmEnabled) {
                            IconButton(onClick = onAddAlarmClick) {
                                Icon(Icons.Default.Add, contentDescription = "알림 추가", tint = Color.White)
                            }
                        }
                    }
                )
            },
            bottomBar = {
                AnimatedVisibility(visible = !hasExactAlarmPermission) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        backgroundColor = Color(0xFF37474F),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "정보", tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "정확한 알람을 위해 '알림 및 리마인더' 권한이 필요합니다.",
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = android.net.Uri.parse("package:${context.packageName}")
                                    }
                                    settingsLauncher.launch(intent)
                                }
                            }) {
                                Text("설정", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 전체 알림 설정 카드
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color.White.copy(alpha = 0.3f)
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
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Switch(
                                checked = settings.isMasterEnabled,
                                onCheckedChange = { viewModel.saveMasterSwitch(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    uncheckedThumbColor = Color.LightGray,
                                    checkedTrackColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "날씨 정보를 알려드리는 전체 기능의 사용 여부를 설정합니다.",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // 전체 알림이 켜져있을 때만 보이는 서브 메뉴들
                AnimatedVisibility(visible = settings.isMasterEnabled) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        // 사용자 지정 알림 설정
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("사용자 지정 알림", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("원하는 시간에 날씨 브리핑을 받습니다.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                            Switch(
                                checked = settings.isUserAlarmEnabled,
                                onCheckedChange = { viewModel.saveUserAlarmSwitch(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    uncheckedThumbColor = Color.LightGray,
                                    checkedTrackColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                        }
                        Divider(color = Color.White.copy(alpha = 0.3f))
                        // 스마트 알림 설정
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("스마트 알림", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("비나 눈이 오기 전에 미리 알려드립니다.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                            Switch(
                                checked = settings.isSmartAlarmEnabled,
                                onCheckedChange = { viewModel.saveSmartAlarmSwitch(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    uncheckedThumbColor = Color.LightGray,
                                    checkedTrackColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }

                // 사용자 지정 알림이 켜져 있을 때만 보이는 알람 목록
                AnimatedVisibility(visible = settings.isMasterEnabled && settings.isUserAlarmEnabled) {
                    Column {
                        Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
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
                                    isEnabled = settings.isMasterEnabled && hasExactAlarmPermission
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmCard(alarm: AlarmEntity, onToggle: () -> Unit, onClick: () -> Unit, isEnabled: Boolean = true) {
    val alpha = if (isEnabled) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onClick() },
        backgroundColor = Color.White.copy(alpha = 0.2f * alpha),
        elevation = 0.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val amPm = if (alarm.hour < 12) "오전" else "오후"
                val hour = if (alarm.hour == 0) 12 else if (alarm.hour > 12) alarm.hour - 12 else alarm.hour
                val minute = "%02d".format(alarm.minute)
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = amPm, fontSize = 16.sp, modifier = Modifier.padding(bottom = 6.dp), fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = alpha))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "$hour:$minute", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = alpha))
                }
                
                val infoText = if (alarm.selectedDate != null) {
                     val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                     sdf.format(Date(alarm.selectedDate))
                } else {
                    if (alarm.days.isEmpty()) "반복 없음" else alarm.days.joinToString(" ")
                }
                
                Text(text = infoText, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f * alpha))
            }
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { if (isEnabled) onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.White.copy(alpha = 0.5f)
                ),
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
    viewModel: AlarmViewModel = hiltViewModel()
) {
    var hourInput by remember { mutableStateOf("") }
    var minuteInput by remember { mutableStateOf("") }
    var isAm by remember { mutableStateOf(true) }

    var isRepeatingMode by remember { mutableStateOf(true) }
    val daysOfWeek = listOf("월", "화", "수", "목", "금", "토", "일")
    val selectedDays = remember { mutableStateListOf<String>() }
    
    var currentAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    
    LaunchedEffect(alarmId) {
        if (alarmId != -1) {
            val alarm = viewModel.getAlarmById(alarmId)
            if (alarm != null) {
                currentAlarm = alarm
                if (alarm.hour == 0) { hourInput = "12"; isAm = true }
                else if (alarm.hour == 12) { hourInput = "12"; isAm = false }
                else if (alarm.hour > 12) { hourInput = (alarm.hour - 12).toString(); isAm = false }
                else { hourInput = alarm.hour.toString(); isAm = true }
                minuteInput = "%02d".format(alarm.minute)
                if (alarm.selectedDate != null) { isRepeatingMode = false; selectedDateMillis = alarm.selectedDate }
                else { isRepeatingMode = true; selectedDays.clear(); selectedDays.addAll(alarm.days) }
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
            today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)
        ).apply { datePicker.minDate = today.timeInMillis }
    }

    val dateText = if (selectedDateMillis != null) {
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        sdf.format(Date(selectedDateMillis!!))
    } else "날짜를 선택하세요"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueGradient)
    ) {
        Scaffold(
            backgroundColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(if (alarmId == -1) "알림 추가" else "알림 수정", color = Color.White) },
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "취소", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            val hInput = hourInput.toIntOrNull() ?: -1
                            val mInput = minuteInput.toIntOrNull() ?: 0
                            if (hInput in 1..12 && mInput in 0..59) {
                                var finalHour = hInput
                                if (isAm) { if (finalHour == 12) finalHour = 0 }
                                else { if (finalHour != 12) finalHour += 12 }

                                if (alarmId == -1) {
                                    viewModel.addAlarm(finalHour, mInput, if (isRepeatingMode) selectedDays.toList() else emptyList(), if (!isRepeatingMode) selectedDateMillis else null)
                                } else {
                                    viewModel.updateAlarmInfo(alarmId, finalHour, mInput, if (isRepeatingMode) selectedDays.toList() else emptyList(), if (!isRepeatingMode) selectedDateMillis else null)
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
                Text("시간 설정", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(end = 16.dp)) {
                        AmPmButton(text = "오전", isSelected = isAm) { isAm = true }
                        Spacer(modifier = Modifier.height(8.dp))
                        AmPmButton(text = "오후", isSelected = !isAm) { isAm = false }
                    }

                    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = hourInput,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.isEmpty()) hourInput = ""
                            else {
                                val num = filtered.toIntOrNull() ?: 0
                                if (num > 12) hourInput = "12" else hourInput = filtered.take(2)
                            }
                        },
                        label = { Text("시") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        colors = textFieldColors,
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp)
                    )
                    
                    Text(" : ", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                    
                    OutlinedTextField(
                        value = minuteInput,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.isEmpty()) minuteInput = ""
                            else {
                                val num = filtered.toIntOrNull() ?: 0
                                if (num > 59) minuteInput = "59" else minuteInput = filtered.take(2)
                            }
                        },
                        label = { Text("분") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        colors = textFieldColors,
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp)
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { isRepeatingMode = true },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (isRepeatingMode) Color.White else Color.White.copy(alpha = 0.3f),
                            contentColor = if (isRepeatingMode) Color(0xFF60A5FA) else Color.White
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                        elevation = ButtonDefaults.elevation(0.dp)
                    ) { Text("요일 반복") }
                    Button(
                        onClick = { isRepeatingMode = false },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (!isRepeatingMode) Color.White else Color.White.copy(alpha = 0.3f),
                            contentColor = if (!isRepeatingMode) Color(0xFF60A5FA) else Color.White
                        ),
                        shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                        elevation = ButtonDefaults.elevation(0.dp)
                    ) { Text("날짜 지정") }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isRepeatingMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("반복할 요일 선택", color = Color.White)
                        TextButton(onClick = {
                            if (selectedDays.size == daysOfWeek.size) selectedDays.clear() else { selectedDays.clear(); selectedDays.addAll(daysOfWeek) }
                        }) {
                            Text(if (selectedDays.size == daysOfWeek.size) "전체 해제" else "전체 선택", color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeek.forEach { day ->
                            val isSelected = selectedDays.contains(day)
                            DayToggleButton(day = day, isSelected = isSelected) { if (isSelected) selectedDays.remove(day) else selectedDays.add(day) }
                        }
                    }
                } else {
                    Text("날짜 선택", color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.3f), contentColor = Color.White),
                        elevation = ButtonDefaults.elevation(0.dp)
                    ) {
                        Text(text = dateText, fontSize = 16.sp, modifier = Modifier.padding(8.dp))
                    }
                }
                
                if (alarmId != -1 && currentAlarm != null) {
                    Spacer(modifier = Modifier.height(56.dp))
                    OutlinedButton(
                        onClick = { currentAlarm?.let { viewModel.deleteAlarm(it) }; navController.popBackStack() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White, backgroundColor = Color.Transparent),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) { Text("이 알림 삭제") }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AmPmButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
            contentColor = if (isSelected) Color(0xFF60A5FA) else Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(width = 60.dp, height = 40.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DayToggleButton(day: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f)
    val contentColor = if (isSelected) Color(0xFF60A5FA) else Color.White

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor, contentColor = contentColor),
        shape = CircleShape,
        modifier = Modifier.size(40.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Text(text = day, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PermissionRequestDialog(
    hasNotificationPermission: Boolean,
    hasExactAlarmPermission: Boolean,
    onDismiss: () -> Unit,
    onConfirmNotification: () -> Unit,
    onConfirmExactAlarm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("권한 안내", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("정확한 날씨 알림을 받으려면 아래의 '알림'과 '리마인더' 권한이 모두 필요합니다.", fontSize = 14.sp)
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                PermissionStatusItem(
                    permissionName = "알림",
                    description = "날씨 정보를 알림으로 받습니다.",
                    isGranted = hasNotificationPermission,
                    buttonText = "권한 허용",
                    onClick = onConfirmNotification
                )
                PermissionStatusItem(
                    permissionName = "리마인더 (정확한 알람)",
                    description = "지정한 시간에 정확히 알림을 울립니다.",
                    isGranted = hasExactAlarmPermission,
                    buttonText = "설정으로 이동",
                    onClick = onConfirmExactAlarm
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun PermissionStatusItem(
    permissionName: String,
    description: String,
    isGranted: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(permissionName, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        if (!isGranted) {
            Button(onClick = onClick, shape = RoundedCornerShape(8.dp)) {
                Text(buttonText)
            }
        } else {
            Text(
                text = "허용됨",
                color = Color(0xFF00C853), // Green A700
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}