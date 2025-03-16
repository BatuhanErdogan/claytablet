package com.mehrphilalethes.claytablet.model

data class TabletAction(
    val actionType: ActionType,
    val wedges: List<WedgeSymbol> // List supports multiple wedges (for clear action)
)

enum class ActionType { ADD, ERASE, CLEAR }
