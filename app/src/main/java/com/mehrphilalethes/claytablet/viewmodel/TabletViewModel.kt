package com.mehrphilalethes.claytablet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mehrphilalethes.claytablet.model.WedgeSymbol
import com.mehrphilalethes.claytablet.model.TransliterationEngine

class TabletViewModel : ViewModel() {

    private val _wedges = mutableListOf<WedgeSymbol>()
    private val transliterationEngine = TransliterationEngine()

    fun addWedge(wedge: WedgeSymbol) {
        _wedges.add(wedge)
    }

    fun getWedges(): List<WedgeSymbol> {
        return _wedges
    }

    fun transliterate(): String {
        // Initially returns placeholder transliteration
        return transliterationEngine.transliterate(_wedges)
    }

    fun clearWedges() {
        _wedges.clear()
    }
}

