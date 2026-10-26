package com.gcoded.meditrack.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.StringRes
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.AdherenceReport
import com.gcoded.meditrack.domain.model.DayAdherence
import com.gcoded.meditrack.domain.model.DayAdherenceStatus
import com.gcoded.meditrack.domain.model.DoseSlotStatus
import com.gcoded.meditrack.domain.model.MedicationAdherence
import com.gcoded.meditrack.domain.model.incidents
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdherenceReportPdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun write(report: AdherenceReport, generatedOn: LocalDate, output: OutputStream) {
        val document = PdfDocument()
        try {
            ReportRenderer(context, document, report, generatedOn).render()
            document.writeTo(output)
        } finally {
            document.close()
        }
    }
}

// A4 at 72 dpi, in PDF points.
private const val PAGE_WIDTH = 595
private const val PAGE_HEIGHT = 842
private const val MARGIN = 40f
private const val CONTENT_WIDTH = 515f
private const val CONTENT_BOTTOM = 792f
private const val ROW_HEIGHT = 22f

private val BRAND = 0xFF006A65.toInt()
private val TEXT = 0xFF1C1B1F.toInt()
private val MUTED = 0xFF5F6368.toInt()
private val DIVIDER = 0xFFDADCE0.toInt()
private val SUMMARY_BOX = 0xFFE0F2F1.toInt()
private val GREEN = 0xFF2E7D32.toInt()
private val AMBER = 0xFFF2A900.toInt()
private val RED = 0xFFC62828.toInt()
private val PENDING_GREY = 0xFFCFD8DC.toInt()
private val LIGHT_GREY = 0xFFF1F3F4.toInt()

private class ReportRenderer(
    context: Context,
    private val document: PdfDocument,
    private val report: AdherenceReport,
    private val generatedOn: LocalDate
) {

    private val resources = context.resources
    private val locale: Locale = Locale.getDefault()
    private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    private val percentFormat = NumberFormat.getPercentInstance(locale)

    private lateinit var page: PdfDocument.Page
    private lateinit var canvas: Canvas
    private var pageNumber = 0
    private var y = MARGIN

    private val titlePaint = paint(22f, bold = true, color = BRAND)
    private val headingPaint = paint(14f, bold = true, color = BRAND)
    private val bodyPaint = paint(11f)
    private val bodyRightPaint = paint(11f, align = Paint.Align.RIGHT)
    private val boldBodyPaint = paint(11f, bold = true)
    private val mutedPaint = paint(9.5f, color = MUTED)
    private val mutedRightPaint = paint(9.5f, color = MUTED, align = Paint.Align.RIGHT)
    private val mutedCenterPaint = paint(9.5f, color = MUTED, align = Paint.Align.CENTER)
    private val tableHeaderPaint = paint(9.5f, bold = true, color = MUTED)
    private val bigNumberPaint = paint(30f, bold = true, color = BRAND)
    private val cellTextPaint = paint(11f, bold = true, align = Paint.Align.CENTER)
    private val monthPaint = paint(6.5f)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DIVIDER
        strokeWidth = 0.8f
    }

    fun render() {
        startPage()
        drawHeader()
        drawSummary()
        drawMedicationTable()
        drawCalendar()
        drawIncidents()
        drawDisclaimer()
        finishPage()
    }

    // ---------- Sections ----------

    private fun drawHeader() {
        val start = report.days.first().date
        val end = report.days.last().date
        line(resources.getString(R.string.report_pdf_title), titlePaint)
        line(
            resources.getString(
                R.string.report_pdf_period_format,
                start.format(dateFormatter),
                end.format(dateFormatter),
                report.days.size
            ),
            bodyPaint
        )
        line(
            resources.getString(R.string.report_pdf_generated_format, generatedOn.format(dateFormatter)),
            mutedPaint
        )
        gap(14f)
    }

    private fun drawSummary() {
        ensureSpace(90f)
        val top = y
        val rate = report.rate
        fillPaint.color = SUMMARY_BOX
        canvas.drawRoundRect(RectF(MARGIN, top, MARGIN + CONTENT_WIDTH, top + 72f), 12f, 12f, fillPaint)

        val rateText = rate?.let { percentFormat.format(it.toDouble()) } ?: "\u2014"
        canvas.drawText(rateText, MARGIN + 18f, top + 45f, bigNumberPaint)

        val textX = MARGIN + 150f
        canvas.drawText(resources.getString(R.string.history_adherence_title), textX, top + 26f, boldBodyPaint)
        val detail = if (rate == null) {
            resources.getString(R.string.history_adherence_none)
        } else {
            resources.getString(R.string.today_progress_format, report.takenCount, report.resolvedCount)
        }
        canvas.drawText(detail, textX, top + 43f, bodyPaint)
        if (report.currentStreakDays > 0) {
            val streak = resources.getQuantityString(
                R.plurals.history_streak_days,
                report.currentStreakDays,
                report.currentStreakDays
            )
            canvas.drawText(streak, textX, top + 60f, bodyPaint)
        }
        y = top + 72f + 18f
    }

    private fun drawMedicationTable() {
        heading(R.string.report_pdf_section_medications)
        if (report.perMedication.isEmpty()) {
            line(resources.getString(R.string.history_adherence_none), mutedPaint)
            gap(12f)
            return
        }

        ensureSpace(ROW_HEIGHT)
        val headerBaseline = y + 14f
        canvas.drawText(resources.getString(R.string.report_pdf_col_medication), MARGIN, headerBaseline, tableHeaderPaint)
        canvas.drawText(resources.getString(R.string.report_pdf_col_dosage), MARGIN + 195f, headerBaseline, tableHeaderPaint)
        canvas.drawText(resources.getString(R.string.report_pdf_col_taken), MARGIN + 305f, headerBaseline, tableHeaderPaint)
        canvas.drawText(resources.getString(R.string.report_pdf_col_rate), MARGIN + 365f, headerBaseline, tableHeaderPaint)
        y += ROW_HEIGHT
        rowDivider()

        report.perMedication.forEach { drawMedicationRow(it) }
        gap(12f)
    }

    private fun drawMedicationRow(item: MedicationAdherence) {
        ensureSpace(ROW_HEIGHT)
        val baseline = y + 15f
        canvas.drawText(fit(item.medication.name, bodyPaint, 190f), MARGIN, baseline, bodyPaint)
        canvas.drawText(fit(item.medication.dosage, bodyPaint, 100f), MARGIN + 195f, baseline, bodyPaint)
        canvas.drawText("${item.taken} / ${item.resolved}", MARGIN + 305f, baseline, bodyPaint)

        val barLeft = MARGIN + 365f
        val barWidth = 90f
        fillPaint.color = LIGHT_GREY
        canvas.drawRoundRect(RectF(barLeft, y + 6f, barLeft + barWidth, y + 14f), 4f, 4f, fillPaint)
        fillPaint.color = GREEN
        canvas.drawRoundRect(RectF(barLeft, y + 6f, barLeft + barWidth * item.rate, y + 14f), 4f, 4f, fillPaint)

        canvas.drawText(
            percentFormat.format(item.rate.toDouble()),
            MARGIN + CONTENT_WIDTH,
            baseline,
            bodyRightPaint
        )
        y += ROW_HEIGHT
        rowDivider()
    }

    private fun drawCalendar() {
        val lead = report.days.first().date.dayOfWeek.value - 1
        val cells: List<DayAdherence?> = List<DayAdherence?>(lead) { null } + report.days
        val weeks = cells.chunked(7)
        val cellHeight = 28f
        val rowStep = cellHeight + 3f

        gap(4f)
        ensureSpace(60f + 18f + weeks.size * rowStep + 40f)
        heading(R.string.history_section_calendar)

        val cellWidth = CONTENT_WIDTH / 7f
        DayOfWeek.entries.forEachIndexed { index, day ->
            canvas.drawText(
                day.getDisplayName(TextStyle.NARROW, locale),
                MARGIN + index * cellWidth + cellWidth / 2f,
                y + 12f,
                mutedCenterPaint
            )
        }
        y += 18f

        weeks.forEach { week ->
            week.forEachIndexed { index, day ->
                if (day != null) drawCalendarCell(index, day, cellWidth, cellHeight)
            }
            y += rowStep
        }
        gap(6f)
        drawLegend()
        gap(12f)
    }

    private fun drawCalendarCell(index: Int, day: DayAdherence, cellWidth: Float, cellHeight: Float) {
        val left = MARGIN + index * cellWidth + 1.5f
        val right = left + cellWidth - 3f
        fillPaint.color = day.status.fillColor()
        canvas.drawRoundRect(RectF(left, y, right, y + cellHeight), 6f, 6f, fillPaint)

        val textColor = day.status.textColor()
        cellTextPaint.color = textColor
        canvas.drawText(day.date.dayOfMonth.toString(), (left + right) / 2f, y + cellHeight / 2f + 6f, cellTextPaint)

        val isRangeStart = day.date == report.days.first().date
        if (isRangeStart || day.date.dayOfMonth == 1) {
            monthPaint.color = textColor
            canvas.drawText(day.date.month.getDisplayName(TextStyle.SHORT, locale), left + 4f, y + 9f, monthPaint)
        }
    }

    private fun drawLegend() {
        ensureSpace(20f)
        var x = MARGIN
        val statuses = listOf(
            DayAdherenceStatus.ALL_TAKEN,
            DayAdherenceStatus.PARTIAL,
            DayAdherenceStatus.NONE_TAKEN,
            DayAdherenceStatus.PENDING,
            DayAdherenceStatus.NO_DOSES
        )
        statuses.forEach { status ->
            fillPaint.color = status.fillColor()
            canvas.drawRoundRect(RectF(x, y + 3f, x + 11f, y + 14f), 3f, 3f, fillPaint)
            val label = resources.getString(status.labelRes())
            canvas.drawText(label, x + 16f, y + 12f, mutedPaint)
            x += 16f + mutedPaint.measureText(label) + 16f
        }
        y += 20f
    }

    private fun drawIncidents() {
        heading(R.string.report_pdf_section_incidents)
        val incidents = report.incidents
        if (incidents.isEmpty()) {
            line(resources.getString(R.string.report_pdf_no_incidents), mutedPaint)
            gap(12f)
            return
        }

        ensureSpace(ROW_HEIGHT)
        val headerBaseline = y + 14f
        canvas.drawText(resources.getString(R.string.report_pdf_col_date), MARGIN, headerBaseline, tableHeaderPaint)
        canvas.drawText(resources.getString(R.string.report_pdf_col_time), MARGIN + 115f, headerBaseline, tableHeaderPaint)
        canvas.drawText(resources.getString(R.string.report_pdf_col_medication), MARGIN + 185f, headerBaseline, tableHeaderPaint)
        canvas.drawText(resources.getString(R.string.report_pdf_col_status), MARGIN + 380f, headerBaseline, tableHeaderPaint)
        y += ROW_HEIGHT
        rowDivider()

        incidents.forEach { slot ->
            ensureSpace(ROW_HEIGHT)
            val baseline = y + 15f
            canvas.drawText(slot.date.format(dateFormatter), MARGIN, baseline, bodyPaint)
            canvas.drawText(slot.time.format(timeFormatter), MARGIN + 115f, baseline, bodyPaint)
            canvas.drawText(fit(slot.medication.name, bodyPaint, 190f), MARGIN + 185f, baseline, bodyPaint)

            val isMissed = slot.status == DoseSlotStatus.MISSED
            val statusPaint = paint(11f, bold = true, color = if (isMissed) RED else MUTED)
            val statusRes = if (isMissed) R.string.dose_status_missed else R.string.dose_status_skipped
            canvas.drawText(resources.getString(statusRes), MARGIN + 380f, baseline, statusPaint)
            y += ROW_HEIGHT
            rowDivider()
        }
        gap(12f)
    }

    private fun drawDisclaimer() {
        val text = resources.getString(R.string.report_pdf_disclaimer)
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, TextPaint(mutedPaint), CONTENT_WIDTH.toInt())
            .build()
        ensureSpace(layout.height.toFloat() + 8f)
        canvas.save()
        canvas.translate(MARGIN, y)
        layout.draw(canvas)
        canvas.restore()
        y += layout.height
    }

    // ---------- Layout helpers ----------

    private fun startPage() {
        pageNumber++
        page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        canvas = page.canvas
        y = MARGIN
    }

    private fun finishPage() {
        drawFooter()
        document.finishPage(page)
    }

    private fun drawFooter() {
        val baseline = PAGE_HEIGHT - 26f
        canvas.drawLine(MARGIN, baseline - 14f, MARGIN + CONTENT_WIDTH, baseline - 14f, dividerPaint)
        canvas.drawText("MediTrack", MARGIN, baseline, mutedPaint)
        canvas.drawText(
            resources.getString(R.string.report_pdf_page_format, pageNumber),
            MARGIN + CONTENT_WIDTH,
            baseline,
            mutedRightPaint
        )
    }

    private fun ensureSpace(height: Float) {
        if (y + height > CONTENT_BOTTOM) {
            finishPage()
            startPage()
        }
    }

    private fun gap(height: Float) {
        y += height
    }

    private fun line(text: String, paint: Paint) {
        val height = paint.textSize * 1.4f
        ensureSpace(height)
        canvas.drawText(text, MARGIN, y + paint.textSize, paint)
        y += height
    }

    private fun heading(@StringRes titleRes: Int) {
        gap(8f)
        // Keep the heading together with at least a table header and one row.
        ensureSpace(70f)
        line(resources.getString(titleRes), headingPaint)
        gap(6f)
    }

    private fun rowDivider() {
        canvas.drawLine(MARGIN, y, MARGIN + CONTENT_WIDTH, y, dividerPaint)
    }

    private fun fit(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "\u2026"
        val count = paint.breakText(text, true, maxWidth - paint.measureText(ellipsis), null)
        return text.take(count).trimEnd() + ellipsis
    }

    private fun paint(
        size: Float,
        bold: Boolean = false,
        color: Int = TEXT,
        align: Paint.Align = Paint.Align.LEFT
    ): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        textAlign = align
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }
}

private fun DayAdherenceStatus.fillColor(): Int = when (this) {
    DayAdherenceStatus.ALL_TAKEN -> GREEN
    DayAdherenceStatus.PARTIAL -> AMBER
    DayAdherenceStatus.NONE_TAKEN -> RED
    DayAdherenceStatus.PENDING -> PENDING_GREY
    DayAdherenceStatus.NO_DOSES -> LIGHT_GREY
}

private fun DayAdherenceStatus.textColor(): Int = when (this) {
    DayAdherenceStatus.ALL_TAKEN, DayAdherenceStatus.NONE_TAKEN -> Color.WHITE
    DayAdherenceStatus.PARTIAL, DayAdherenceStatus.PENDING -> TEXT
    DayAdherenceStatus.NO_DOSES -> MUTED
}

@StringRes
private fun DayAdherenceStatus.labelRes(): Int = when (this) {
    DayAdherenceStatus.ALL_TAKEN -> R.string.history_status_complete
    DayAdherenceStatus.PARTIAL -> R.string.history_status_partial
    DayAdherenceStatus.NONE_TAKEN -> R.string.history_status_missed
    DayAdherenceStatus.PENDING -> R.string.history_status_pending
    DayAdherenceStatus.NO_DOSES -> R.string.history_status_none
}
