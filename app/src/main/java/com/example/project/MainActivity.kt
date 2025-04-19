package com.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.ui.theme.ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WebSocketManager.connect()

        setContent {
            ProjectTheme {
                ChatScreen()
            }
        }
    }
}

@Composable
fun ChatScreen() {
    val salas = listOf("general", "gaming", "off-topic")
    var salaActual by remember { mutableStateOf("general") }

    val mensajesPorSala = remember { mutableStateMapOf<String, MutableList<String>>() }
    var mensaje by remember { mutableStateOf("") }

    val mensajes = mensajesPorSala.getOrPut(salaActual) { mutableListOf() }

    // Escuchar mensajes entrantes por WebSocket
    LaunchedEffect(salaActual) {
        WebSocketManager.onMessageReceived = { msg ->
            mensajes.add("Amigo: $msg")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF313338))
    ) {
        // Barra superior con selector de sala
        TopAppBar(
            title = {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    salas.forEach { sala ->
                        Button(
                            onClick = { salaActual = sala },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (sala == salaActual) Color(0xFF5865F2) else Color.Transparent,
                                contentColor = Color.White
                            )
                        ) {
                            Text("#$sala")
                        }
                    }
                }
            },
            backgroundColor = Color(0xFF2B2D31)
        )

        // Lista de mensajes
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            reverseLayout = true
        ) {
            items(mensajes.reversed()) { msg ->
                Row(modifier = Modifier.padding(vertical = 6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (msg.startsWith("Tú:")) "Tú" else "Amigo",
                            color = Color(0xFFb5bac1),
                            fontSize = 14.sp
                        )
                        Text(
                            text = msg.removePrefix("Tú: ").removePrefix("Amigo: "),
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Input de mensaje
        Row(
            modifier = Modifier
                .padding(8.dp)
                .background(Color(0xFF383A40), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = mensaje,
                onValueChange = { mensaje = it },
                placeholder = { Text("Escribe un mensaje...", color = Color(0xFFb5bac1)) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.White,
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                if (mensaje.isNotBlank()) {
                    WebSocketManager.sendMessage(mensaje)
                    mensajes.add("Tú: $mensaje")
                    mensaje = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}
