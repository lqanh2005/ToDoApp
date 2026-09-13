package com.lqanh.todoandroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lqanh.todoandroid.data.AppAssets
import com.lqanh.todoandroid.ui.theme.Error
import com.lqanh.todoandroid.ui.theme.ErrorContainer
import com.lqanh.todoandroid.ui.theme.OnSurface
import com.lqanh.todoandroid.ui.theme.OnSurfaceVariant
import com.lqanh.todoandroid.ui.theme.Outline
import com.lqanh.todoandroid.ui.theme.Primary
import com.lqanh.todoandroid.ui.theme.PrimaryContainer
import com.lqanh.todoandroid.ui.theme.PrimaryFixed
import com.lqanh.todoandroid.ui.theme.Secondary
import com.lqanh.todoandroid.ui.theme.SecondaryFixed
import com.lqanh.todoandroid.ui.theme.SurfaceContainerLow
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    hapticEnabled: Boolean,
    onHapticChange: (Boolean) -> Unit,
    onResetData: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var notifications by remember { mutableStateOf(true) }
    var aiHelper by remember { mutableStateOf(true) }
    var showSaved by remember { mutableStateOf(false) }

    if (showSaved) {
        LaunchedEffect(Unit) {
            delay(2000)
            showSaved = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 100.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = AppAssets.PROFILE_AVATAR,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLow)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            if (userName.isBlank()) "Chưa cập nhật tên" else userName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (userName.isBlank()) Outline else OnSurface
                        )
                        Text(
                            if (userEmail.isBlank()) "Chưa cập nhật email" else userEmail,
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryFixed)
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tài khoản Cá nhân", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Primary)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Họ và tên", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập họ và tên") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Primary
                    ),
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                Text("Địa chỉ Email", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập địa chỉ email") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Primary
                    ),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Lưu thông tin",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary)
                        .clickable { showSaved = true }
                        .padding(vertical = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Cài đặt ứng dụng", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                SettingToggle(
                    icon = { Icon(Icons.Default.Notifications, null, tint = Primary) },
                    iconBg = SurfaceContainerLow,
                    title = "Thông báo nhắc hẹn",
                    subtitle = "Báo động trước giờ thực hiện",
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )
                SettingToggle(
                    icon = { Icon(Icons.Default.Psychology, null, tint = Secondary) },
                    iconBg = SecondaryFixed,
                    title = "Gợi ý trợ lý AI",
                    subtitle = "Tối ưu phân phối lời nhắc thông minh",
                    checked = aiHelper,
                    onCheckedChange = { aiHelper = it }
                )
                SettingToggle(
                    icon = { Icon(Icons.Default.Vibration, null, tint = Primary) },
                    iconBg = SurfaceContainerLow,
                    title = "Phản hồi rung chạm",
                    subtitle = "Hiệu ứng xúc giác khi hoàn thành",
                    checked = hapticEnabled,
                    onCheckedChange = onHapticChange
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ErrorContainer.copy(0.6f))
                        .clickable(onClick = onResetData)
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.RestartAlt, null, tint = Error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Khôi phục dữ liệu mẫu ban đầu", color = Error, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                Text("ToDoList • Phiên bản 2.4.0 (Material Design 3)", fontSize = 11.sp, color = Outline)
            }
        }

        if (showSaved) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp, start = 24.dp, end = 24.dp)
            ) {
                Text("Đã lưu thay đổi thành công!")
            }
        }
    }
}

@Composable
private fun SettingToggle(
    icon: @Composable () -> Unit,
    iconBg: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBg),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
            Text(subtitle, fontSize = 11.sp, color = OnSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Primary, checkedThumbColor = Color.White)
        )
    }
}
