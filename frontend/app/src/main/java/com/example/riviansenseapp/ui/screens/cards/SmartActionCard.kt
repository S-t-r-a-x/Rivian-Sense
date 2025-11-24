package com.example.riviansenseapp.ui.screens.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.riviansenseapp.context.SmartAction

/**
 * Smart Action Card - prikazuje kontekstualne akcije
 */
@Composable
fun SmartActionCard(
    smartAction: SmartAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = 0.3f),
                        Color(0xFF3B82F6).copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - icon and content
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon
                Text(
                    text = smartAction.icon,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                
                // Text content
                Column {
                    Text(
                        text = smartAction.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    
                    Text(
                        text = smartAction.description,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8), // slate-400
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    // Reason badge
                    if (smartAction.priority <= 2) {
                        Text(
                            text = "⚡ ${smartAction.reason}",
                            fontSize = 10.sp,
                            color = Color(0xFF06B6D4), // cyan
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(
                                    Color(0xFF06B6D4).copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            

        }
    }
}
