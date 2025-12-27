package com.example.tennisapp.session

import android.content.Context
import com.example.tennisapp.bluetooth.IMUData
import com.example.tennisapp.sensor.kpi.KPIState
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionRecorder(private val context: Context) {

    private var rawWriter: BufferedWriter? = null
    private var kpiWriter: BufferedWriter? = null
    private var hitWriter: BufferedWriter? = null

    private var sessionDir: File? = null
    private var rawFile: File? = null
    private var kpiFile: File? = null
    private var hitsFile: File? = null

    private var activeSessionId: String? = null

    fun start(): SessionFiles {
        stop()

        val root = File(context.filesDir, "sessions")
        if (!root.exists()) root.mkdirs()

        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        activeSessionId = stamp

        sessionDir = File(root, stamp)
        sessionDir!!.mkdirs()

        rawFile = File(sessionDir!!, "raw.csv")
        kpiFile = File(sessionDir!!, "kpi.csv")
        hitsFile = File(sessionDir!!, "hits.csv")

        rawWriter = BufferedWriter(OutputStreamWriter(rawFile!!.outputStream()))
        kpiWriter = BufferedWriter(OutputStreamWriter(kpiFile!!.outputStream()))
        hitWriter = BufferedWriter(OutputStreamWriter(hitsFile!!.outputStream()))

        rawWriter!!.write("timestamp,ax_mg,ay_mg,az_mg,gx_cdps,gy_cdps,gz_cdps")
        rawWriter!!.newLine()

        kpiWriter!!.write("timestamp,accMag,impact,apex,angularSpeed,spinRpm,power")
        kpiWriter!!.newLine()

        hitWriter!!.write("timestamp,power,spinRpm,impactIntensity")
        hitWriter!!.newLine()

        return SessionFiles(
            sessionId = stamp,
            dir = sessionDir!!,
            raw = rawFile!!,
            kpi = kpiFile!!,
            hits = hitsFile!!
        )
    }

    fun appendRaw(d: IMUData) {
        val w = rawWriter ?: return
        w.write("${d.timestamp},${d.ax},${d.ay},${d.az},${d.gx},${d.gy},${d.gz}")
        w.newLine()
    }

    fun appendKpi(k: KPIState) {
        val w = kpiWriter ?: return
        w.write("${k.timestamp},${k.accelMagnitude},${k.impactDetected},${k.apexDetected},${k.angularSpeed},${k.spinRPM},${k.estimatedPower}")
        w.newLine()
    }

    fun appendHit(h: HitRecord) {
        val w = hitWriter ?: return
        w.write("${h.timestamp},${h.power},${h.spinRpm},${h.impactIntensity}")
        w.newLine()
    }

    fun writeSummary(summary: SessionSummary) {
        val dir = sessionDir ?: return
        val f = File(dir, "summary.json")

        val obj = JSONObject()
        obj.put("sessionId", summary.sessionId)
        obj.put("startTimeMs", summary.startTimeMs)
        obj.put("endTimeMs", summary.endTimeMs)
        obj.put("durationMs", summary.durationMs)
        obj.put("hitCount", summary.hitCount)

        obj.put("avgPower", summary.avgPower)
        obj.put("maxPower", summary.maxPower)
        obj.put("minPower", summary.minPower)

        obj.put("avgSpin", summary.avgSpin)
        obj.put("maxSpin", summary.maxSpin)
        obj.put("minSpin", summary.minSpin)

        f.writeText(obj.toString())
    }

    fun stop() {
        rawWriter?.flush()
        rawWriter?.close()
        rawWriter = null

        kpiWriter?.flush()
        kpiWriter?.close()
        kpiWriter = null

        hitWriter?.flush()
        hitWriter?.close()
        hitWriter = null

        sessionDir = null
        rawFile = null
        kpiFile = null
        hitsFile = null
        activeSessionId = null
    }
}

data class SessionFiles(
    val sessionId: String,
    val dir: File,
    val raw: File,
    val kpi: File,
    val hits: File
)
