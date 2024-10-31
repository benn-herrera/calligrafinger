package com.tinybitsinteractive.calligrafinger.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tinybitsinteractive.calligrafinger.ui.theme.CalligrafingerTheme

@Composable
fun MainUI(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        WritingSurface()
        Row {
            Button(
                content = { Text("Control A") },
                onClick = {
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainUIPreview() {
    CalligrafingerTheme {
        MainUI()
    }
}
