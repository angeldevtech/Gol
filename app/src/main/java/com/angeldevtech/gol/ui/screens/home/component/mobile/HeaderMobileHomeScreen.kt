package com.angeldevtech.gol.ui.screens.home.component.mobile

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.angeldevtech.gol.R

@Composable
fun HeaderMobileHomeScreen(
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
        }

        IconButton(onClick = { showLanguageDialog = true }) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = stringResource(R.string.select_language)
            )
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialogMobile(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { langCode ->
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
                AppCompatDelegate.setApplicationLocales(appLocale)
                showLanguageDialog = false
            }
        )
    }
}

@Composable
fun LanguageSelectionDialogMobile(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_language)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val languages = listOf(
                    "es" to R.string.lang_es,
                    "en" to R.string.lang_en,
                    "pt" to R.string.lang_pt,
                    "fr" to R.string.lang_fr,
                    "de" to R.string.lang_de,
                    "it" to R.string.lang_it
                )
                languages.forEach { (code, nameRes) ->
                    TextButton(
                        onClick = { onLanguageSelected(code) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(nameRes))
                    }
                }
            }
        },
        confirmButton = {}
    )
}