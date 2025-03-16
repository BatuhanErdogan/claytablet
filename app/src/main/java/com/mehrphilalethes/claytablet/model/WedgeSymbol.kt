package com.mehrphilalethes.claytablet.model

data class WedgeSymbol(
    val type: WedgeType,      // enum: HORIZONTAL, VERTICAL, DIAGONAL, WINKELHAKEN
    val x: Float,             // position on tablet
    val y: Float,
    val rotation: Float,       // orientation of wedge
    val length: Float,
    val thickness: Float,
    val size: Float,
    val thresholdSensitivity: Float
)
