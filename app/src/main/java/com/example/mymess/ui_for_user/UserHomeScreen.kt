package com.example.mymess.ui_for_user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun UserHomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Today's Menu",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            )

            MenuCard(
                title = "Breakfast",
                items = "Aloo Paratha, Curd, Tea",
                time = "8:00 AM - 10:00 AM",
                color = Color(0xFFFFCC80) 
            )

            MenuCard(
                title = "Lunch",
                items = "Rice, Dal Fry, Seasonal Vegetable, Roti, Salad",
                time = "12:30 PM - 2:30 PM",
                color = Color(0xFFFFAB91) 
            )

            MenuCard(
                title = "Dinner",
                items = "Paneer Butter Masala, Roti, Rice, Gulab Jamun",
                time = "7:30 PM - 9:30 PM",
                color = Color(0xFFCE93D8) 
            )
        }
    }
}

@Composable
fun MenuCard(title: String, items: String, time: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = items,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
            }
        }
    }
}
