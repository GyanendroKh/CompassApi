package io.gyanendrokh.test.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import io.gyanendrokh.test.compass.ui.theme.CompassTheme
import kotlin.math.PI
import kotlin.math.abs

@ExperimentalUnitApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompassTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    App()
                }
            }
        }
    }
}

@ExperimentalUnitApi
@Composable
fun App() {
    val lifecycle = LocalLifecycleOwner.current
    val ctx = LocalContext.current

    val accelerometerReading = remember { mutableStateOf<FloatArray?>(null) }
    val magnetometerReading = remember { mutableStateOf<FloatArray?>(null) }

    val orientationAngles = remember { mutableStateOf<FloatArray?>(null) }

    DisposableEffect(lifecycle) {
        val sensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val eventListener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                if (e.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val arr = FloatArray(3)
                    System.arraycopy(
                        e.values,
                        0,
                        arr,
                        0,
                        arr.size
                    )

                    accelerometerReading.value = arr
                } else if (e.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    val arr = FloatArray(3)
                    System.arraycopy(
                        e.values,
                        0,
                        arr,
                        0,
                        arr.size
                    )

                    magnetometerReading.value = arr
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            }
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                eventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            sensorManager.registerListener(
                eventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        val sides = listOf(
            Pair(0f, "N"),
            Pair(45f, "NE"),
            Pair(90f, "E"),
            Pair(135f, "SE"),
            Pair(180f, "S"),
            Pair(225f, "SW"),
            Pair(270f, "W"),
            Pair(315f, "NW"),
        )

        for (i in sides.indices) {
            val prev = sides[if (i == 0) sides.size - 1 else i - 1].first
            val curr = sides[i]
            val next = if (i != sides.size - 1) sides[i + 1].first else 360f

            if (i == 0) {
                val prevMid = (prev + 360f) / 2
                val nextMid = (curr.first + next) / 2

                Log.d("CompassRange", "Range: $prevMid $nextMid")
            } else {
                val prevMid = (prev + curr.first) / 2
                val nextMid = (curr.first + next) / 2

                Log.d("CompassRange", "Range: $prevMid $nextMid")
            }
        }

        onDispose {
            sensorManager.unregisterListener(eventListener)
        }
    }

    LaunchedEffect(accelerometerReading.value, magnetometerReading.value) {
        if (accelerometerReading.value == null || magnetometerReading.value == null) {
            return@LaunchedEffect
        }

        val matrix = FloatArray(9)
        val angles = FloatArray(3)

        SensorManager.getRotationMatrix(
            matrix,
            null,
            accelerometerReading.value,
            magnetometerReading.value
        )
        SensorManager.getOrientation(matrix, angles)

        orientationAngles.value = angles
    }

    if (orientationAngles.value != null) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Azimuthal: ${changeAngle(orientationAngles.value!![0])}",
                style = TextStyle(
                    fontSize = TextUnit(25f, TextUnitType.Sp)
                )
            )
            Box(
                modifier = Modifier
                    .align(CenterHorizontally)
            ) {
                Text(
                    text = calculateDirection(orientationAngles.value!![0]),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit(35f, TextUnitType.Sp)
                    )
                )
            }
        }
    } else {
        Text(text = "Initializing...")
    }
}


fun calculateDirection(anglePi: Float): String {
    /*
     * Azimuth, angle of rotation about the -z axis.
     * When facing north, this angle is 0,
     * when facing south, this angle is π.
     * Likewise, when facing east, this angle is π/2, and
     * when facing west, this angle is -π/2.
     * The range of values is -π to π.
     */
    val sides = listOf(
        Pair(0f, "N"),
        Pair(45f, "NE"),
        Pair(90f, "E"),
        Pair(135f, "SE"),
        Pair(180f, "S"),
        Pair(225f, "SW"),
        Pair(270f, "W"),
        Pair(315f, "NW"),
    )

    val dAnglePi = if (anglePi < 0) PI.toFloat() - abs(anglePi) else anglePi
    var angle = (dAnglePi / (PI.toFloat() - 0f) * (180 - 0f) + 0f)
    if (anglePi < 0) {
        angle += 180f
    }

    for (i in sides.indices) {
        val prev = sides[if (i == 0) sides.size - 1 else i - 1].first
        val curr = sides[i]
        val next = if (i != sides.size - 1) sides[i + 1].first else 360f

        if (i == 0) {
            val prevMid = (prev + 360f) / 2
            val nextMid = (curr.first + next) / 2

            if ((angle in prevMid..360f) || angle >= curr.first && angle <= nextMid) {
                return curr.second
            }
        } else {
            val prevMid = (prev + curr.first) / 2
            val nextMid = (curr.first + next) / 2

            if ((angle >= prevMid && angle <= curr.first) || (angle >= curr.first && angle <= nextMid)) {
                return curr.second
            }
        }
    }

    return "N/A"
}

fun changeAngle(anglePi: Float): Float {
    val dAnglePi = if (anglePi < 0) PI.toFloat() - abs(anglePi) else anglePi
    var angle = (dAnglePi / (PI.toFloat() - 0f) * (180 - 0f) + 0f)
    if (anglePi < 0) {
        angle += 180f
    }

    return angle
}
