package com.example.agriplus.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agriplus.data.FirebaseHistoryManager
import com.example.agriplus.data.HistoryItem
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen() {

    val manager = remember { FirebaseHistoryManager() }
    val scope = rememberCoroutineScope()

    var history by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        history = manager.getResults()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Button(
            onClick = {
                scope.launch {
                    manager.clearAll()
                    history = emptyList()
                }
            }
        ) {
            Text("Clear All")
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            items(history) { item ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {

                    Column(modifier = Modifier.padding(12.dp)) {

                        Text(text = item.result)

                        Text(
                            text = "Confidence: ${(item.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}