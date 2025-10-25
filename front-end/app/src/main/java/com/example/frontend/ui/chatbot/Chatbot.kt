package com.example.frontend.ui.chatbot

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.R

@Composable
fun ChatBotScreen() {

    var textInput by remember {mutableStateOf("")}

    Scaffold(
        topBar = { TopBar() },
        content = { padding ->
            // Sử dụng Column để sắp xếp ChatSection và MessageInput theo chiều dọc
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
            ) {
                // Áp dụng Modifier.weight(1f) cho LazyColumn
                // để nó chiếm hết không gian có sẵn, đẩy MessageInput xuống dưới cùng.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // SỬA LỖI Ở ĐÂY
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        MessageBubble(text = "What should we make?", isFromUser = false)
                        Spacer(modifier = Modifier.height(8.dp))
                        // Đây là nơi bạn sẽ hiển thị các tin nhắn khác
                    }
                }
                // MessageInput sẽ được đặt ở dưới cùng
                MessageInput(
                    text = textInput,
                    onTextChange = {newText -> textInput = newText},
                    onSendClick = {
                        if (textInput.isNotBlank()) {
                            Log.d(TAG, "Tin nhắn đã gửi: ${textInput}")
                            textInput = ""

                        }
                    }
                )
            }
        }
    )
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.spotube), // Thay thế bằng logo của bạn
            contentDescription = "Logo",
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "SpoTube",
            color = Color.Red,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = " AI",
            color = Color.Red,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MessageBubble(text: String, isFromUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromUser) Color(0xFF3C3C3C) else Color.DarkGray
            )
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                color = Color.White
            )
        }
    }
}


@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.Face,
            contentDescription = "Emoji",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text(text = "Message...", color = Color.Gray) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            // SỬA LỖI Ở ĐÂY
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF3C3C3C),    // Màu nền khi được chọn
                unfocusedContainerColor = Color(0xFF3C3C3C),  // Màu nền khi không được chọn
                focusedIndicatorColor = Color.Transparent,    // Bỏ đường viền khi được chọn
                unfocusedIndicatorColor = Color.Transparent,  // Bỏ đường viền khi không được chọn
                cursorColor = Color.White,                    // Màu con trỏ
                focusedTextColor = Color.White                // Màu chữ khi nhập
            ),
            trailingIcon = {
                IconButton(onClick = onSendClick) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.Gray
                    )
                }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ChatBotScreenPreview() {
    ChatBotScreen()
}