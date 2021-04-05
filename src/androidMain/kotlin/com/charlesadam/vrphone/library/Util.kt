package com.charlesadam.vrphone.library

import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier

object Util {
    fun startLogger(){
        Napier.base(DebugAntilog())
        Napier.v("Napier Started")
    }
}