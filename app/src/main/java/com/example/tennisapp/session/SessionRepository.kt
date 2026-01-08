package com.example.tennisapp.session

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File

class SessionRepository(private val context: Context) {

    private fun rootDir(): File = File(context.filesDir, "sessions")

    fun listSessions(): List<SessionSummary> {
        val root = rootDir()
        Log.d("SessionRepository", "Root dir exists: ${root.exists()}, path: ${root.absolutePath}")

        if (!root.exists()) return emptyList()

        val dirs = root.listFiles()?.filter { it.isDirectory } ?: emptyList()
        Log.d("SessionRepository", "Found ${dirs.size} session directories")

        val summaries = dirs.mapNotNull { dir ->
            val f = File(dir, "summary.json")
            Log.d("SessionRepository", "Checking session in: ${dir.name}")

            if (!f.exists()) {
                Log.d("SessionRepository", "No summary.json in ${dir.name}")
                return@mapNotNull null
            }

            try {
                val jsonText = f.readText()
                Log.d("SessionRepository", "JSON content: $jsonText")

                val obj = JSONObject(jsonText)

                val summary = SessionSummary(
                    sessionId = obj.getString("sessionId"),
                    startTimeMs = obj.getLong("startTimeMs"),
                    endTimeMs = obj.getLong("endTimeMs"),
                    durationMs = obj.getLong("durationMs"),
                    hitCount = obj.getInt("hitCount"),

                    avgPower = obj.getDouble("avgPower").toFloat(),
                    maxPower = obj.getDouble("maxPower").toFloat(),
                    minPower = obj.getDouble("minPower").toFloat(),

                    avgSpin = obj.getDouble("avgSpin").toFloat(),
                    maxSpin = obj.getDouble("maxSpin").toFloat(),
                    minSpin = obj.getDouble("minSpin").toFloat(),

                    playerName = obj.optString("playerName", ""),
                    sessionNotes = obj.optString("sessionNotes", "")
                )

                Log.d("SessionRepository", "Loaded session: ${summary.sessionId}, avgSpin: ${summary.avgSpin}, avgPower: ${summary.avgPower}")
                summary

            } catch (e: Exception) {
                Log.e("SessionRepository", "Error loading session ${dir.name}: ${e.message}", e)
                null
            }
        }

        Log.d("SessionRepository", "Total loaded sessions: ${summaries.size}")
        return summaries.sortedByDescending { it.startTimeMs }
    }

    fun loadHits(sessionId: String): List<HitRecord> {
        val dir = File(rootDir(), sessionId)
        val f = File(dir, "hits.csv")
        if (!f.exists()) return emptyList()

        val lines = f.readLines()
        if (lines.size <= 1) return emptyList()

        return lines.drop(1).mapNotNull { line ->
            val p = line.split(",")
            if (p.size < 4) return@mapNotNull null
            HitRecord(
                timestamp = p[0].toLongOrNull() ?: return@mapNotNull null,
                power = p[1].toFloatOrNull() ?: return@mapNotNull null,
                spinRpm = p[2].toFloatOrNull() ?: return@mapNotNull null,
                impactIntensity = p[3].toFloatOrNull() ?: return@mapNotNull null
            )
        }
    }
}