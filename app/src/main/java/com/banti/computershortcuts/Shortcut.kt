package com.banti.computershortcuts

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Shortcut(
    val id: Int,
    val category: String,
    val action: String,
    val windows: String,
    val mac: String,
    val linux: String,
    val description: String
) : Parcelable {
    fun getShortcutForOS(os: String): String {
        return when(os.lowercase()) {
            "windows" -> windows
            "mac" -> mac
            "linux" -> linux
            else -> windows
        }
    }
}
