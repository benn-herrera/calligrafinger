package com.tinybitsinteractive.calligrafinger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tinybitsinteractive.calligrafinger.ui.theme.CalligrafingerTheme

@Composable
fun MainUI(modifier: Modifier = Modifier) {
    var cmd = remember { mutableStateOf(WritingCommand.BlackInk) }
    var clear = remember { mutableStateOf(false) }

    Column(modifier = modifier
        .background(color = Color.DarkGray)
    ) {
        Row (modifier = Modifier
            .align(alignment = CenterHorizontally)
        )
        {
            val buttonMod = Modifier
                .padding(all=4.dp)
                .requiredSize(75.dp, 36.dp)
            Button(
                modifier = buttonMod,
                content = { Text("Blk") },
                onClick = {
                    cmd.value = WritingCommand.BlackInk
                },
                enabled = (cmd.value != WritingCommand.BlackInk)
            )
            Button(
                modifier = buttonMod,
                content = { Text("Blu") },
                onClick = {
                    cmd.value = WritingCommand.BlueInk
                },
                enabled = (cmd.value != WritingCommand.BlueInk)
            )
            Button(
                modifier = buttonMod,
                content = { Text("Red") },
                onClick = {
                    cmd.value = WritingCommand.RedInk
                },
                enabled = (cmd.value != WritingCommand.RedInk)
            )
            Button(
                modifier = buttonMod,
                content = { Text("Ers") },
                onClick = {
                    clear.value = true
                },
                enabled = true
            )
        }
        // NOTE: when button row is first they are visible at the top.
        //       when button row is 2nd Compose does not limit the size of
        //       WritingSurface to leave room for the row.
        if (clear.value) {
            clear.value = false
            WritingSurface(WritingCommand.ClearPage)
        } else {
            WritingSurface(cmd.value)
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
