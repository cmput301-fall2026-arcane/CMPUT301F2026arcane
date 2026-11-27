package com.example.code.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.col

@Composable
fun NotificationCard(
    title: String,
    body: String,
    time: String,
    unread: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = col.HomeButton)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            if (unread) {
                Box(
                    Modifier
                        .padding(top = 6.dp, end = 10.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(col.CampsiteCardOpen)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title, color = col.HomeButtonText, fontSize = 18.sp,
                    fontWeight = if (unread) FontWeight.Bold else FontWeight.Normal
                )
                if (body.isNotEmpty()) Text(body, color = col.HomeButtonText)
                Text(time, color = col.HomeButtonText.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}