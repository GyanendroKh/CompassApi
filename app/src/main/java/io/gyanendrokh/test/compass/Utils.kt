package io.gyanendrokh.test.compass

import kotlin.math.PI

class Utils {
    companion object {
        val SIDES = listOf(
            Pair(0f, "N"),
            Pair(45f, "NE"),
            Pair(90f, "E"),
            Pair(135f, "SE"),
            Pair(180f, "S"),
            Pair(225f, "SW"),
            Pair(270f, "W"),
            Pair(315f, "NW"),
            Pair(360f, "N"),
        )

        val SIDE_RANGES: List<Pair<String, Pair<Float, Float>>>

        init {
            val ranges = mutableListOf<Pair<String, Pair<Float, Float>>>()

            /**
             * For every direction, calculating a angle window
             *
             * eg: For 90deg i.e, E-ast
             *      the prev, 45deg i.e, NE (North East), and
             *      the next, 135deg i.e, SE (South East)
             *
             *      Taking the mid of 45deg and 90deg; 90deg and 135deg,
             *      i.e, 22.5deg and 67.5deg; making this the window for the direction E-ast
             *      any angle falling between the two angles can be taken as the direction E-ast
             */
            for (i in SIDES.indices) {
                val prev = if (i == 0) SIDES[i].first else SIDES[i - 1].first
                val (curr, S) = SIDES[i]
                val next = if (i == SIDES.lastIndex) SIDES[i].first else SIDES[i + 1].first

                val prevMid = (prev + curr) / 2
                val nextMid = (curr + next) / 2

                ranges.add(Pair(S, Pair(prevMid, nextMid)))
            }

            SIDE_RANGES = ranges
        }

        /**
         * Converting Angle in 0 -> PI to 0deg -> 180deg.
         * 0 -> PI -> -PI -> 0 to 0deg -> 180deg -> 360deg
         */
        fun convertAngle(anglePi: Float): Float {
            val dAnglePi = if (anglePi < 0) PI.toFloat() + anglePi else anglePi

            return (dAnglePi / (PI.toFloat() - 0f) * (180 - 0f) + 0f) + if (anglePi < 0) 180f else 0f
        }

        /**
         * Mapping angle given in -PI -> PI to Cardinal Directions or Points
         */
        fun calculateDirection(anglePi: Float): String {
            val angle = convertAngle(anglePi)

            for ((S, R) in SIDE_RANGES) {
                if (angle in R.first.rangeTo(R.second)) {
                    return S
                }
            }

            return "N/A"
        }
    }
}
