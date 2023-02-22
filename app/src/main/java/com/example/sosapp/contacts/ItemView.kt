package com.example.sosapp.contacts

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExtendedFloatingActionButton
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
fun ItemView(name: String, phone: String) {

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .fillMaxHeight()
            .border(2.dp, Color.Black,RoundedCornerShape(50.dp) )
            .padding(10.dp)
            .combinedClickable(onLongClick = AlertClick()),
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

@Composable
fun AlertClick() {


    AlertDialog(onDismissRequest = {},
        title = { Text(stringResource(id = R.string.remove_title)) },
        text = { Text(stringResource(id = R.string.remove_text)) },
        confirmButton = {
            ExtendedFloatingActionButton(text = {
                Text(
                    modifier = Modifier.wrapContentSize(), text = stringResource(id = R.string.yes), color = Color.White
                )
            }, shape = RoundedCornerShape(16.dp), onClick = {
                // delete the specified contact from the database
                db.deleteContact(c)
                // remove the item from the list
                contacts.remove(c)
                // notify the listview that dataset has been changed
                notifyDataSetChanged()
                Toast.makeText(context, "Contact removed!", Toast.LENGTH_SHORT).show()
            })
        },
        dismissButton = {
            ExtendedFloatingActionButton(text = {
                Text(
                    modifier = Modifier.wrapContentSize(), text = stringResource(id = R.string.no), color = Color.White
                )
            }, shape = RoundedCornerShape(16.dp), onClick = {})
        })
}

