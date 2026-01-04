package com.example.tennisapp.session

import android.content.Context
import org.json.JSONObject
import java.io.File

class SessionRepository(private val context: Context) {

    private fun rootDir(): File = File(context.filesDir, "sessions")

    fun listSessions(): List<SessionSummary> {
        val root = rootDir()
        if (!root.exists()) return emptyList()

        val dirs = root.listFiles()?.filter { it.isDirectory } ?: emptyList()

        val summaries = dirs.mapNotNull { dir ->
            val f = File(dir, "summary.json")
            if (!f.exists()) return@mapNotNull null

            val obj = JSONObject(f.readText())

            SessionSummary(
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
        }

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
