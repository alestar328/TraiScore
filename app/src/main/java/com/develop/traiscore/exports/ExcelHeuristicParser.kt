package com.develop.traiscore.exports

import android.content.Context
import android.net.Uri
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipInputStream

data class ParsedExercise(
    val name: String,
    val series: Int,
    val reps: String,
    val weight: String,
    val rir: Int = 0
)

data class ParsedDivision(
    val name: String,
    val exercises: MutableList<ParsedExercise> = mutableListOf()
)

object ExcelHeuristicParser {

    fun parse(context: Context, uri: Uri): List<ParsedDivision> {
        val mime = context.contentResolver.getType(uri) ?: ""
        return if (mime == "text/csv" || uri.lastPathSegment?.endsWith(".csv", true) == true) {
            parseCsv(context, uri)
        } else {
            parseXlsx(context, uri)
        }
    }

    // ─── CSV ──────────────────────────────────────────────────────────────────

    private fun parseCsv(context: Context, uri: Uri): List<ParsedDivision> {
        val lines = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()?.readLines() ?: return emptyList()
        val rows = lines.map { line ->
            line.split(",", ";", "\t").map { it.trim().removeSurrounding("\"") }
        }
        return applyHeuristics(rows)
    }

    // ─── XLSX ─────────────────────────────────────────────────────────────────

    private fun parseXlsx(context: Context, uri: Uri): List<ParsedDivision> {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
        var sharedStrings: List<String> = emptyList()
        val sheetBytes = mutableMapOf<String, ByteArray>()

        ZipInputStream(inputStream.buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when (entry.name) {
                    "xl/sharedStrings.xml" ->
                        sharedStrings = parseSharedStrings(zip.readBytes().inputStream())
                    else -> if (entry.name.matches(Regex("xl/worksheets/sheet\\d+\\.xml"))) {
                        sheetBytes[entry.name] = zip.readBytes()
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        val allRows = mutableListOf<List<String>>()
        sheetBytes.keys.sorted().forEach { name ->
            val rows = parseSheet(sheetBytes[name]!!.inputStream(), sharedStrings)
            if (rows.isNotEmpty()) {
                allRows.addAll(rows)
                allRows.add(emptyList()) // separador entre hojas
            }
        }
        return applyHeuristics(allRows)
    }

    private fun parseSharedStrings(input: InputStream): List<String> {
        val result = mutableListOf<String>()
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(input, null)
        }
        val sb = StringBuilder()
        var inT = false
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "si" -> sb.clear()
                    "t" -> inT = true
                }
                XmlPullParser.TEXT -> if (inT) sb.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "t" -> inT = false
                    "si" -> result.add(sb.toString())
                }
            }
            event = parser.next()
        }
        return result
    }

    private fun parseSheet(input: InputStream, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(input, null)
        }
        var currentRow = mutableListOf<Pair<Int, String>>()
        var inCell = false
        var inValue = false
        var cellType = ""
        var cellRef = ""
        val cellValue = StringBuilder()
        var event = parser.eventType

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "row" -> currentRow = mutableListOf()
                    "c" -> {
                        inCell = true
                        cellType = parser.getAttributeValue(null, "t") ?: ""
                        cellRef = parser.getAttributeValue(null, "r") ?: ""
                        cellValue.clear()
                    }
                    "v", "t" -> if (inCell) inValue = true
                }
                XmlPullParser.TEXT -> if (inValue) cellValue.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "v", "t" -> inValue = false
                    "c" -> {
                        val colIdx = refToCol(cellRef)
                        val value = when (cellType) {
                            "s" -> sharedStrings.getOrNull(
                                cellValue.toString().toIntOrNull() ?: -1
                            ) ?: ""
                            "b" -> if (cellValue.toString() == "1") "TRUE" else "FALSE"
                            else -> cellValue.toString()
                        }
                        if (value.isNotBlank()) currentRow.add(colIdx to value)
                        inCell = false
                    }
                    "row" -> {
                        if (currentRow.isNotEmpty()) {
                            val maxCol = currentRow.maxOf { it.first }
                            val row = Array(maxCol + 1) { "" }
                            currentRow.forEach { (col, v) -> row[col] = v }
                            rows.add(row.toList())
                        }
                    }
                }
            }
            event = parser.next()
        }
        return rows
    }

    /** "B3" → 1, "AA1" → 26 */
    private fun refToCol(ref: String): Int =
        ref.takeWhile { it.isLetter() }
            .uppercase()
            .fold(0) { acc, c -> acc * 26 + (c - 'A' + 1) } - 1

    // ─── HEURISTICS ──────────────────────────────────────────────────────────

    private fun applyHeuristics(rows: List<List<String>>): List<ParsedDivision> {
        val colMap = detectColumnMap(rows)
        val headerRowIndex = rows.indexOfFirst { isHeaderRow(it) }
        val divisions = mutableListOf<ParsedDivision>()
        var current: ParsedDivision? = null

        rows.forEachIndexed { index, row ->
            if (index == headerRowIndex) return@forEachIndexed
            val nonEmpty = row.filter { it.isNotBlank() }
            if (nonEmpty.isEmpty()) return@forEachIndexed

            when {
                isDivisionHeader(row) -> {
                    current = ParsedDivision(name = nonEmpty.first().trim())
                    divisions.add(current!!)
                }
                else -> {
                    val ex = parseExerciseRow(row, colMap) ?: return@forEachIndexed
                    if (current == null) {
                        current = ParsedDivision(name = "División 1")
                        divisions.add(current!!)
                    }
                    current!!.exercises.add(ex)
                }
            }
        }
        return divisions.filter { it.exercises.isNotEmpty() }
    }

    private data class ColMap(
        val name: Int = 0,
        val series: Int = 1,
        val reps: Int = 2,
        val weight: Int = 3,
        val rir: Int = -1
    )

    private fun detectColumnMap(rows: List<List<String>>): ColMap {
        val header = rows.firstOrNull { isHeaderRow(it) } ?: return ColMap()
        var n = 0; var s = 1; var r = 2; var w = 3; var rir = -1
        header.forEachIndexed { i, cell ->
            val l = cell.lowercase()
            when {
                l.contains("ejercicio") || l.contains("exercise") || l.contains("nombre") -> n = i
                l.contains("serie") || l == "s" -> s = i
                l.contains("rep") -> r = i
                l.contains("peso") || l.contains("weight") || l == "kg" -> w = i
                l.contains("rir") -> rir = i
            }
        }
        return ColMap(n, s, r, w, rir)
    }

    private fun isHeaderRow(row: List<String>): Boolean {
        val kw = setOf("ejercicio", "exercise", "serie", "rep", "peso", "weight", "kg", "rir", "nombre")
        val nonEmpty = row.filter { it.isNotBlank() }
        if (nonEmpty.size < 2) return false
        return nonEmpty.count { cell -> kw.any { cell.lowercase().contains(it) } } >= 2
    }

    private fun isDivisionHeader(row: List<String>): Boolean {
        val nonEmpty = row.filter { it.isNotBlank() }
        if (nonEmpty.isEmpty() || nonEmpty.size > 3) return false
        val main = nonEmpty.first()
        if (main.toDoubleOrNull() != null) return false
        val lw = main.lowercase()
        val keywords = listOf(
            "día", "dia", "day", "push", "pull",
            "pierna", "pecho", "espalda", "hombro", "brazo",
            "leg", "chest", "back", "shoulder", "arm",
            "core", "cardio", "full body", "fullbody",
            "tren superior", "tren inferior", "upper", "lower",
            "sesión", "sesion", "bloque", "división", "division",
            "glute", "glúteo", "abs", "abdomen"
        )
        val hasKeyword = keywords.any { lw.contains(it) }
        val hasNumbers = nonEmpty.any { it.toDoubleOrNull() != null }
        // Single text cell ≥4 chars with no numbers = likely header
        return hasKeyword || (!hasNumbers && nonEmpty.size == 1 && main.length >= 4)
    }

    private fun parseExerciseRow(row: List<String>, map: ColMap): ParsedExercise? {
        val name = row.getOrNull(map.name)?.trim() ?: return null
        if (name.isBlank() || name.toDoubleOrNull() != null || name.length < 3) return null
        val series = row.getOrNull(map.series)?.trim()?.toIntOrNull() ?: 3
        val reps = row.getOrNull(map.reps)?.trim()?.ifBlank { "10" } ?: "10"
        val weight = row.getOrNull(map.weight)?.trim()?.replace(",", ".") ?: "0"
        val rir = if (map.rir >= 0) row.getOrNull(map.rir)?.toIntOrNull() ?: 0 else 0
        return ParsedExercise(
            name = name.lowercase().split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
            series = series.coerceIn(1, 20),
            reps = reps,
            weight = weight.ifBlank { "0" },
            rir = rir.coerceIn(0, 5)
        )
    }

    /** Infiere el tipo de rutina (DefaultCategoryExer) desde el nombre de la división. */
    fun inferType(divisionName: String): String {
        val lw = divisionName.lowercase()
        return when {
            listOf("pecho", "chest", "push", "press banca").any { lw.contains(it) } -> "CHEST"
            listOf("espalda", "back", "pull", "dominada", "jalón", "remo").any { lw.contains(it) } -> "BACK"
            listOf("pierna", "leg", "sentadilla", "squat", "cuádricep", "isquio", "femoral").any { lw.contains(it) } -> "LEGS"
            listOf("glute", "glúteo", "trasero", "hip thrust").any { lw.contains(it) } -> "GLUTES"
            listOf("brazo", "arm", "bícep", "trícep", "curl").any { lw.contains(it) } -> "ARMS"
            listOf("hombro", "shoulder", "deltoid", "press militar", "press arnold").any { lw.contains(it) } -> "SHOULDERS"
            listOf("core", "abdomen", "abs", "plancha", "abdominal").any { lw.contains(it) } -> "CORE"
            else -> "BACK"
        }
    }
}
