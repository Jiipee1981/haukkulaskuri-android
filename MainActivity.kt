package fi.srloy.haukkulaskuri

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private val results = mutableListOf<Int>()
    private var running = false
    private var recorder: AudioRecord? = null
    private var barkCount = 0
    private var lastBarkMs = 0L
    private lateinit var status: TextView
    private lateinit var resultText: TextView
    private lateinit var start: Button
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40,60,40,40)
        }
        val title = TextView(this).apply { text="HAUKKULASKURI"; textSize=30f }
        status = TextView(this).apply { text="Valmis mittaukseen"; textSize=22f }
        resultText = TextView(this).apply { text="Otos 1: –\nOtos 2: –\nOtos 3: –\n\nKeskiarvo: –"; textSize=24f }
        start = Button(this).apply { text="ALOITA 60 S OTOS"; setOnClickListener { begin() } }
        val reset = Button(this).apply { text="NOLLAA"; setOnClickListener { results.clear(); refresh(); status.text="Valmis mittaukseen" } }
        box.addView(title); box.addView(status); box.addView(start); box.addView(resultText); box.addView(reset)
        setContentView(box)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 10)
    }

    private fun begin() {
        if (running || results.size >= 3) return
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return
        running = true; barkCount = 0
        start.isEnabled = false
        status.text = "Otos ${results.size+1} käynnissä – 60 s"
        val sr=16000
        val min=AudioRecord.getMinBufferSize(sr, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        recorder=AudioRecord(MediaRecorder.AudioSource.MIC,sr,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,min*2)
        recorder!!.startRecording()
        Thread {
            val buf=ShortArray(min)
            val end=System.currentTimeMillis()+60000
            while(running && System.currentTimeMillis()<end) {
                val n=recorder!!.read(buf,0,buf.size)
                if(n>0) {
                    var sum=0.0
                    for(i in 0 until n) sum += buf[i].toDouble()*buf[i]
                    val rms=sqrt(sum/n)
                    val now=System.currentTimeMillis()
                    // Prototype threshold + refractory period. Tune with real elk-hound recordings.
                    if(rms>5000 && now-lastBarkMs>220) { barkCount++; lastBarkMs=now }
                }
            }
            stopSample()
        }.start()
    }

    private fun stopSample() {
        running=false
        try { recorder?.stop(); recorder?.release() } catch(_:Exception){}
        recorder=null
        handler.post {
            results.add(barkCount)
            status.text="Otos ${results.size}: $barkCount haukkua/min"
            start.isEnabled = results.size < 3
            refresh()
        }
    }

    private fun refresh() {
        val lines=(0..2).joinToString("\n") { i -> "Otos ${i+1}: ${if(i<results.size) "${results[i]} haukkua/min" else "–"}" }
        val avg=if(results.isNotEmpty()) String.format("%.1f",results.average()) else "–"
        resultText.text="$lines\n\nKeskiarvo: $avg${if(results.isNotEmpty()) " haukkua/min" else ""}"
        start.text=if(results.size<3) "ALOITA 60 S OTOS ${results.size+1}" else "KOLME OTTOA VALMIS"
    }

    override fun onDestroy() { running=false; try { recorder?.stop(); recorder?.release() } catch(_:Exception){}; super.onDestroy() }
}
