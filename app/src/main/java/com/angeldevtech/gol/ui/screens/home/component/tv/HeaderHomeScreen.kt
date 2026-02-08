package com.angeldevtech.gol.ui.screens.home.component.tv

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.angeldevtech.gol.R
import com.angeldevtech.gol.ui.screens.home.HomeUIState

@Composable
fun HeaderHomeScreen(
    uiState: HomeUIState,
    refresh: () -> Unit,
    refreshButtonFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
){
    var showLanguageDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { showLanguageDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = stringResource(R.string.select_language)
                )
            }

            if (uiState is HomeUIState.Success) {
                Button(
                    onClick = refresh,
                    modifier = Modifier.focusRequester(refreshButtonFocusRequester)
                ) {
                    Text(
                        text = stringResource(R.string.refresh)
                    )
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialogTV(
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
fun LanguageSelectionDialogTV(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    androidx.tv.material3.Surface(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.tv.material3.ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.headlineSmall
            )

            val languages = listOf(
                "es" to R.string.lang_es,
                "en" to R.string.lang_en,
                "pt" to R.string.lang_pt,
                "fr" to R.string.lang_fr,
                "de" to R.string.lang_de,
                "it" to R.string.lang_it
            )

            languages.forEach { (code, nameRes) ->
                Button(
                    onClick = { onLanguageSelected(code) },
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    Text(text = stringResource(nameRes))
                }
            }
        }
    }
}