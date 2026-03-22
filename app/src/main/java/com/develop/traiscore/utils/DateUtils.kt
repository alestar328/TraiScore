package com.develop.traiscore.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Formato canónico de fecha en toda la app: "dd/MM/yyyy" */
val DATE_FORMAT: SimpleDateFormat
    get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

/** Devuelve la fecha de hoy formateada como "dd/MM/yyyy" */
fun todayFormatted(): String = DATE_FORMAT.format(Calendar.getInstance().time)

/** Formatea cualquier [Date] como "dd/MM/yyyy" */
fun Date.toDisplayDate(): String = DATE_FORMAT.format(this)
