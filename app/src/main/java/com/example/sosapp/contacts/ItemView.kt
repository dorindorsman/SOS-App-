package com.example.sosapp.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sosapp.R

@Composable
fun ItemView(name: String, phone: String, showDialog: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .fillMaxHeight()
            .border(2.dp, Color.Black,RoundedCornerShape(50.dp) )
            .padding(10.dp)
            .clickable (onClick = { showDialog() }),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier
            .background(color = Color.White)
        ) {
            Icon(
                modifier = Modifier.weight(0.2f),
                imageVector = Icons.Default.Person,
                tint = Color.Black,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(id = R.string.name),
                modifier = Modifier.weight(0.5f),
                style = TextStyle(fontSize = 18.sp, color = Color.Black)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontSize = 18.sp, color = Color.Black)
            )
        }
        Row(modifier = Modifier
            .wrapContentWidth()
            .background(color = Color.White)
        ) {
            Icon(
                modifier = Modifier.weight(0.2f),
                imageVector = Icons.Default.Phone,
                tint = Color.Black,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(id = R.string.phone),
                modifier = Modifier.weight(0.5f),
                style = TextStyle(fontSize = 18.sp, color = Color.Black)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = phone,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontSize = 18.sp, color = Color.Black)
            )
        }
    }
}


