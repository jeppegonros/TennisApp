package com.example.tennisapp.view

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tennisapp.session.SessionSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    sessions: List<SessionSummary>,
    onBack: () -> Unit,
    onOpenSession: (String) -> Unit
) {
    val dateFmt = SimpleDateFormat("yyyy MMM dd, HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No saved sessions yet")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sessions) { s ->
                val dateText = dateFmt.format(Date(s.startTimeMs))
                SessionSummaryCard(
                    summary = s,
                    dateText = dateText,
                    onClick = { onOpenSession(s.sessionId) } // sessionId used internally only
                )
            }
        }
    }
}

@Composable
private fun SessionSummaryCard(
    summary: SessionSummary,
    dateText: String,
    onClick: () -> Unit
) {
    val minutes = (summary.durationMs / 60000.0).roundToInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(dateText, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Duration $minutes min, Hits ${summary.hitCount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Icon(Icons.Filled.ArrowForward, contentDescription = "Open")
            }

            Divider()

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Power", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Avg ${fmt1(summary.avgPower)}, Max ${fmt1(summary.maxPower)}, Min ${fmt1(summary.minPower)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Spin", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Avg ${fmt0(summary.avgSpin)} rpm, Max ${fmt0(summary.maxSpin)} rpm, Min ${fmt0(summary.minSpin)} rpm",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(2.dp))
        }
    }
}

private fun fmt1(v: Float): String = String.format(Locale.US, "%.1f", v)
private fun fmt0(v: Float): String = String.format(Locale.US, "%.0f", v)
