package de.dhbw.demo

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val raplaParser = RaplaParser()
        val requestQueue = Volley.newRequestQueue(this)

        validateCalendarPermission()

        setContent {

            var buttonEnabled by remember { mutableStateOf(true) }
            var raplaEvents by remember { mutableStateOf<List<RaplaEvent>>(emptyList()) }

            if (raplaEvents.isEmpty()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(enabled = buttonEnabled, onClick = {
                        buttonEnabled = false
                        val request = StringRequest(
                            Request.Method.GET,
                            "...",
                            { response ->
                                Log.d(
                                    MainActivity::class.java.simpleName,
                                    "Response is '$response'"
                                )

                                val parserResult = raplaParser.parse(response)
                                val events = parserResult?.weeks?.flatMap { it.events }
                                raplaEvents = events ?: emptyList()

                                buttonEnabled = true
                            }, {
                                Log.e(MainActivity::class.java.simpleName, "An error occurred!")
                                buttonEnabled = true
                            })
                        requestQueue.add(request)
                    }) {
                        Text("Show calendar!")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxSize()
                ) {

                    Text(
                        text = "Upcoming events",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(raplaEvents) { event ->
                            EventListItem(
                                event.title,
                                event.course ?: "<no course title provided>",
                                event.date,
                                event.startTime,
                                event.endTime,
                                event.room ?: "<no room provided>",
                                {}
                            )
                        }
                    }
                }
            }
        }
    }

    private fun validateCalendarPermission() {
        val readCalendar =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
        val writeCalendar =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)

        if (PackageManager.PERMISSION_DENIED == readCalendar || PackageManager.PERMISSION_DENIED == writeCalendar) {
            val calendarPermissionRequest =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                    {
                        Log.d(MainActivity::class.java.simpleName, "Permissions $permissions")
                    }
                }

            calendarPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
                )
            )

        }
    }
}
