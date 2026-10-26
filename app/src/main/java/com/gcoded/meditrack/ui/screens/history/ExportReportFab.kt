package com.gcoded.meditrack.ui.screens.history

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gcoded.meditrack.R

/** Exports the displayed range as a PDF and opens the system share sheet. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExportReportFab(
    range: HistoryRange?,
    modifier: Modifier = Modifier,
    viewModel: ExportReportViewModel = hiltViewModel()
) {
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.export_share_chooser_title)
    val subject = stringResource(R.string.export_share_subject)
    val errorMessage = stringResource(R.string.export_error_message)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ExportEvent.Ready ->
                    context.startActivity(shareIntent(event.uri, subject, chooserTitle))
                ExportEvent.Failed ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    if (range == null) return

    ExtendedFloatingActionButton(
        onClick = { if (!isExporting) viewModel.onExportClick(range) },
        icon = {
            if (isExporting) {
                LoadingIndicator(modifier = Modifier.size(28.dp))
            } else {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
            }
        },
        text = {
            Text(
                stringResource(
                    if (isExporting) R.string.export_generating else R.string.export_report_fab
                )
            )
        },
        modifier = modifier
    )
}

private fun shareIntent(uri: Uri, subject: String, chooserTitle: String): Intent {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        clipData = ClipData.newRawUri(null, uri)
    }
    return Intent.createChooser(send, chooserTitle)
}
