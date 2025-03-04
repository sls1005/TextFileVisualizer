package test.sls1005.projects.textfilevisualizer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import test.sls1005.projects.textfilevisualizer.ui.theme.TextFileVisualizerTheme

class AboutActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TextFileVisualizerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("About", fontWeight = FontWeight.Bold)
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        Text(
                            buildAnnotatedString {
                                val url = "https://github.com/sls1005/TextFileVisualizer"
                                append("The source code of this app is available at ")
                                withLink(
                                    LinkAnnotation.Url(url, styles = TextLinkStyles(SpanStyle(color = Color(0xFF3792FA)))),
                                ) { append(url) }
                                append(", under the MIT License")
                            },
                            fontSize = 20.sp,
                            lineHeight = 26.sp,
                            modifier = Modifier.padding(20.dp)
                        )
                        TextButton(
                            onClick = {
                                startActivity(
                                    Intent(this@AboutActivity, ShowOpenSourceLibrariesActivity::class.java)
                                )
                            }, modifier = Modifier.fillMaxWidth().padding(10.dp)
                        ) {
                            Text("See open source libraries used", fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}