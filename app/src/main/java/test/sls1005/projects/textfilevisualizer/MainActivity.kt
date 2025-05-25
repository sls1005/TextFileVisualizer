package test.sls1005.projects.textfilevisualizer

import android.content.Intent
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import test.sls1005.projects.textfilevisualizer.ui.theme.TextFileVisualizerTheme

class MainActivity : ComponentActivity() {
    private enum class CharType(
        public val code: Int
    ) {
        SP(0x20),
        NBSP(0xa0),
        CR(0xd),
        LF(0xa),
        HT(0x9),
        VT(0xb),
        TEXT(-1),
        BOM(-2),
        ZWNBSP(0xfeff)
    }
    private fun getCharType(c: Char): CharType {
        for (t in CharType.entries) {
            if (t.code == c.code && t.code > 0) {
                return t
            }
        }
        return CharType.TEXT
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TextFileVisualizerTheme {
                val charLabel = listOf(
                    Pair(CharType.SP, "SP"),
                    Pair(CharType.NBSP, "NBSP"),
                    Pair(CharType.CR, "CR"),
                    Pair(CharType.LF, "LF"),
                    Pair(CharType.HT, "HT"),
                    Pair(CharType.VT, "VT"),
                    Pair(CharType.TEXT, "TEXT"),
                    Pair(CharType.BOM, "BOM"),
                    Pair(CharType.ZWNBSP, "ZWNBSP")
                ).toMap()
                val color = listOf(
                    Pair(CharType.SP, Color(0xFF8DDEFF)),
                    Pair(CharType.NBSP, Color(0xFFA7E6FF)),
                    Pair(CharType.CR, Color(0xFFFBAD84)),
                    Pair(CharType.LF, Color(0xFFFF9070)),
                    Pair(CharType.HT, Color(0xFF5E85FF)),
                    Pair(CharType.VT, Color(0xFFFF353B)),
                    Pair(CharType.TEXT, Color(0xFFC495FF)),
                    Pair(CharType.BOM, Color(0xFFFFFF20)),
                    Pair(CharType.ZWNBSP, Color(0xFFFFFF20))
                ).toMap()
                var value by remember { mutableStateOf(TextFieldValue()) }
                val text /* : String */ by remember { derivedStateOf { value.text } }
                var textIsMonospaced by remember { mutableStateOf(false) }
                var fullscreenEnabled by remember { mutableStateOf(false) }
                var showsIndentationTool by remember { mutableStateOf(false) }
                var showsSummary by remember { mutableStateOf(false) }
                if (showsSummary) {
                    Dialog(
                        onDismissRequest = { showsSummary = false }
                    ) {
                        Card(
                            modifier = Modifier
                                .width(300.dp)
                                .wrapContentHeight()
                                .sizeIn(minHeight = 300.dp, maxHeight = 400.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text("Summary",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
                            )
                            val anyTextSelected by remember { derivedStateOf { value.selection.length != 0 } }
                            val (wordCount, unicodeCharCount) = wordCountAndUnicodeCharacterCount(
                                if (anyTextSelected) {
                                    val i1 by remember { derivedStateOf { value.selection.min } }
                                    val i2 by remember { derivedStateOf { value.selection.max } }
                                    text.slice(i1 ..< i2)
                                } else {
                                    text
                                }
                            )
                            Text(
                                stringResource(
                                    if (anyTextSelected) {
                                        R.string.selected_colon
                                    } else {
                                        R.string.total_colon
                                    }
                                ) + "\n\n" + stringResource(R.string.summary_template,
                                    wordCount,
                                    unicodeCharCount,
                                    text.toByteArray(Charsets.UTF_8).size
                                ),
                                fontSize = 30.sp,
                                lineHeight = 35.sp,
                                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, start = 15.dp, end = 10.dp)
                            )
                        }
                    }
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (!fullscreenEnabled) {
                            TopAppBar(
                                title = {
                                    Text("")
                                },
                                actions = {
                                    IconButton(
                                        onClick = { fullscreenEnabled = true }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.fullscreen),
                                            "Full-screen mode"
                                        )
                                    }
                                    if (textIsMonospaced) {
                                        OutlinedIconButton(
                                            onClick = { textIsMonospaced = false }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.m_space),
                                                "Monospaced text (On)"
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = { textIsMonospaced = true }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.m_space),
                                                "Monospaced text (Off)"
                                            )
                                        }
                                    }
                                    var showsMenu by remember { mutableStateOf(false) }
                                    IconButton(
                                        onClick = { showsMenu = true }
                                    ) {
                                        Icon(
                                            Icons.Filled.MoreVert,
                                            contentDescription = "Options"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showsMenu,
                                        onDismissRequest = { showsMenu = false }
                                    ) {
                                        if (text.isNotEmpty()) {
                                            if (text[0].code != CharType.ZWNBSP.code) {
                                                DropdownMenuItem(
                                                    text = { Text("Add BOM (byte-order mark)", fontSize = 20.sp, lineHeight = 22.sp, modifier = Modifier.padding(5.dp)) },
                                                    onClick = {
                                                        value = TextFieldValue("\ufeff$text")
                                                        showsMenu = false
                                                    }
                                                )
                                            }
                                        }
                                        DropdownMenuItem(
                                            text = { Text(
                                                    if (showsIndentationTool) {
                                                        "Hide indentation tool"
                                                    } else {
                                                        "Indentation & Outdentation"
                                                    }, fontSize = 20.sp,
                                                       lineHeight = 22.sp,
                                                       modifier = Modifier.padding(5.dp)
                                                ) },
                                            onClick = {
                                                showsIndentationTool = !showsIndentationTool
                                                showsMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Summary", fontSize = 20.sp, lineHeight = 22.sp, modifier = Modifier.padding(5.dp)) },
                                            onClick = {
                                                showsSummary = true
                                                showsMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("About", fontSize = 20.sp, lineHeight = 22.sp, modifier = Modifier.padding(5.dp)) },
                                            onClick = {
                                                startActivity(
                                                    Intent(this@MainActivity, AboutActivity::class.java)
                                                )
                                                showsMenu = false
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        var charForIndentation by remember { mutableStateOf('\t') }
                        val charTypeForIndentation by remember { derivedStateOf { getCharType(charForIndentation) } }
                        var indentationCharNum by remember { mutableIntStateOf(1) }
                        if (showsIndentationTool && (!fullscreenEnabled)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            ) {
                                var showsDialogForSettingIndentationCharNum by remember { mutableStateOf(false) }
                                if (showsDialogForSettingIndentationCharNum) {
                                    var input by remember { mutableStateOf("") }
                                    input = "$indentationCharNum"
                                    Dialog(
                                        onDismissRequest = {
                                            val s = input
                                            var backwardCounter = run { // = allowed max input length
                                                var x = Int.MAX_VALUE
                                                var k = 0
                                                while (x != 0) { // 1 + int(log 10)
                                                    x = x / 10
                                                    k += 1
                                                }
                                                (k)
                                            }
                                            var errorFlag = false
                                            val validatedInput = buildString(s.length) {
                                                for (c in s) {
                                                    if (backwardCounter == 0) {
                                                        break
                                                    }
                                                    if (c.isDigit()) {
                                                        append(c)
                                                    } else if (c !in listOf(' ', ',', '.', '\'', '+', '-', '\u00b7')) {
                                                        errorFlag = true
                                                        break
                                                    }
                                                    backwardCounter -= 1
                                                }
                                            }
                                            if (!errorFlag) {
                                                try {
                                                    validatedInput.toInt().also {
                                                        if (it > 0) {
                                                            indentationCharNum = it
                                                        }
                                                    }
                                                } catch (_: NumberFormatException) {
                                                    // do nothing
                                                }
                                            }
                                            showsDialogForSettingIndentationCharNum = false
                                        }
                                    ) {
                                        Card(
                                            modifier = Modifier.wrapContentSize()
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("Enter the number of tabs/spaces used for indentation", fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp))
                                                OutlinedTextField(
                                                    input,
                                                    textStyle = TextStyle(fontSize = 30.sp),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    onValueChange = { input = it },
                                                    modifier = Modifier.padding(10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Button( // Close
                                    onClick = { showsIndentationTool = false },
                                    shape = RectangleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = color[charTypeForIndentation] ?: Color(0xFF5E85FF),
                                        contentColor = Color(0xFF000000)
                                    ),
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .padding(5.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.x),
                                        "Hide indentation tool"
                                    )
                                }
                                Button( // Number
                                    onClick = { showsDialogForSettingIndentationCharNum = true },
                                    shape = RectangleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = color[charTypeForIndentation] ?: Color(0xFF5E85FF),
                                        contentColor = Color(0xFF000000)
                                    ),
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .padding(5.dp)
                                ) {
                                    val label by remember { derivedStateOf { "$indentationCharNum" } }
                                    Text(label)
                                }
                                Button( // Char
                                    onClick = {
                                        charForIndentation = if (charForIndentation == '\t') { ' ' } else { '\t' }
                                    },
                                    shape = RectangleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = color[charTypeForIndentation] ?: Color(0xFF5E85FF),
                                        contentColor = Color(0xFF000000)
                                    ),
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .padding(5.dp)
                                ) {
                                    Text(charLabel[charTypeForIndentation]!!)
                                }
                                Button( // Dedent
                                    onClick = {
                                        val i1 = value.selection.min
                                        val i2 = value.selection.max
                                        var startIndex = i1
                                        var endIndexExclusive = i2
                                        var newCursorOffset = 0
                                        val indentationChar = charForIndentation
                                        if (i1 > 0) {
                                            for (i in (i1 - 1) downTo 0) {
                                                val c = text[i]
                                                if (c == '\n') {
                                                    startIndex = i + 1
                                                    break
                                                } else if (i == 0) {
                                                    startIndex = 0
                                                }
                                            }
                                        }
                                        if (text.length > i2) {
                                            val i3 = i2 + if (i2 > 0) { -1 } else { 0 }
                                            for (i in i3 ..< text.length) {
                                                if (text[i] == '\n') {
                                                    break
                                                }
                                                endIndexExclusive = i + 1
                                            }
                                        }
                                        value = TextFieldValue(
                                            buildString {
                                                append(text.slice(0 ..< startIndex))
                                                if (text.length >= endIndexExclusive) {
                                                    var state = 1
                                                    var counter = 0
                                                    for (i in startIndex ..< endIndexExclusive) {
                                                        val c = text[i]
                                                        if (c == '\n') {
                                                            state = 1
                                                            counter = 0
                                                            append(c)
                                                        } else if (c == charForIndentation && state == 1) {
                                                            if (counter < indentationCharNum) {
                                                                newCursorOffset -= 1
                                                                counter += 1
                                                            } else {
                                                                state = 2
                                                                append(c)
                                                            }
                                                        } else { // c != '\n' && (c != charForIndentation || state != 1)
                                                            if (c.code == CharType.ZWNBSP.code && i == 0) { // BOM
                                                                state = 1
                                                            } else {
                                                                state = 2
                                                            }
                                                            append(c)
                                                        }
                                                    }
                                                    if (text.length > endIndexExclusive) {
                                                        append(text.slice(endIndexExclusive ..< text.length))
                                                    }
                                                }
                                            },
                                            selection = TextRange(
                                                max(min(i1 + newCursorOffset, text.length + 1), 0),
                                                max(min(i2 + newCursorOffset * (1 + text.slice(i1 ..< i2).count({ it == '\n' })), text.length + 1), 0)
                                            )
                                        )
                                    },
                                    shape = RectangleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = color[charTypeForIndentation] ?: Color(0xFF5E85FF),
                                        contentColor = Color(0xFF000000)
                                    ),
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .padding(5.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.dedent),
                                        "Dedent"
                                    )
                                }
                                Button( // Indent
                                    onClick = {
                                        val i1 = value.selection.min
                                        val i2 = value.selection.max
                                        var startIndex = i1
                                        var endIndexExclusive = i2
                                        var newCursorOffset = indentationCharNum
                                        val secondCursorOffset = newCursorOffset * (1 + text.slice(i1 ..< i2).count({ it == '\n' }))
                                        if (i1 > 0) {
                                            for (i in (i1 - 1) downTo 0) {
                                                if (text[i] == '\n') {
                                                    startIndex = i + 1
                                                    break
                                                } else if (i == 0) {
                                                    startIndex = 0
                                                }
                                            }
                                        }
                                        if (text.length > i2) {
                                            val i3 = i2 + if (i2 > 0) { -1 } else { 0 }
                                            for (i in i3 ..< text.length) {
                                                if (text[i] == '\n') {
                                                    break
                                                }
                                                endIndexExclusive = i + 1
                                            }
                                        }
                                        value = TextFieldValue(
                                            buildString {
                                                append(text.slice(0 ..< startIndex))
                                                if (startIndex == 0 && text.length > 0) {
                                                    val c = text[0]
                                                    if (c.code == CharType.ZWNBSP.code) { // BOM
                                                        append(c)
                                                        startIndex += 1
                                                    }
                                                }
                                                for (unusedLoopVar in 0 ..< indentationCharNum) {
                                                    append(charForIndentation)
                                                }
                                                if (text.length >= endIndexExclusive) {
                                                    for (i in startIndex ..< endIndexExclusive) {
                                                        val c = text[i]
                                                        if (c == '\n') {
                                                            append("\n")
                                                            for (unusedLoopVar in 0 ..< indentationCharNum) {
                                                                append(charForIndentation)
                                                            }
                                                        } else {
                                                            append(c)
                                                        }
                                                    }
                                                    if (text.length > endIndexExclusive) {
                                                        append(text.slice(endIndexExclusive ..< text.length))
                                                    }
                                                }
                                            },
                                            selection = TextRange(
                                                max(min(i1 + newCursorOffset, text.length + 1), 0),
                                                max(min(i2 + secondCursorOffset, text.length + secondCursorOffset + 1), 0)
                                            )
                                        )
                                    },
                                    shape = RectangleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = color[charTypeForIndentation] ?: Color(0xFF5E85FF),
                                        contentColor = Color(0xFF000000)
                                    ),
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .padding(5.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.indent),
                                        "Indent"
                                    )
                                }
                            }
                        }
                        var textSize by remember { mutableStateOf(35.sp) }
                        val r = remember { FocusRequester() }
                        OutlinedTextField(
                            value,
                            onValueChange = { value = it },
                            textStyle = TextStyle(
                                fontSize = textSize,
                                fontFamily = if (textIsMonospaced) {
                                    FontFamily.Monospace
                                } else {
                                    FontFamily.Default
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(
                                    if (fullscreenEnabled) {
                                        1.0f
                                    } else if(LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE) {
                                        if (showsIndentationTool) {
                                            0.75f
                                        } else {
                                            0.8f
                                        }
                                    } else if (showsIndentationTool) {
                                        0.85f
                                    } else {
                                        0.86f
                                    }
                                )
                                .focusRequester(r)
                                .transformable(
                                    rememberTransformableState { f1, _, _ ->
                                        textSize = (textSize * f1).let { result ->
                                            if (result < 10.sp) {
                                                10.sp
                                            } else {
                                                result
                                            }
                                        }
                                    }
                                )
                        )
                        LaunchedEffect(Unit) {
                            delay(5)
                            r.requestFocus()
                        }
                        BackHandler(fullscreenEnabled) {
                            fullscreenEnabled = false
                        }
                        if (!fullscreenEnabled) {
                            Row (
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier
                                    .wrapContentSize()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                val a = ArrayDeque<CharType>()
                                val i1 by remember { derivedStateOf { value.selection.min } }
                                val i2 by remember { derivedStateOf { value.selection.max } }
                                var startIndex = i1
                                if (value.selection.length == 0) {
                                    if (i1 > 0) {
                                        for (i in (i1 - 1) downTo 0) {
                                            val c = text[i]
                                            if (c == '\n') {
                                                startIndex = i + 1
                                                break
                                            } else {
                                                a.addFirst(
                                                    getCharType(c).let {
                                                        if (it.code == CharType.ZWNBSP.code && i == 0) {
                                                            CharType.BOM
                                                        } else {
                                                            it
                                                        }
                                                    }
                                                )
                                                if (i == 0) {
                                                    startIndex = 0
                                                }
                                            }
                                        }
                                    }
                                    for (i in i1 ..< text.length) {
                                        val c = text[i]
                                        a.addLast(
                                            getCharType(c).let {
                                                if (it.code == CharType.ZWNBSP.code && i == 0) {
                                                    CharType.BOM
                                                } else {
                                                    it
                                                }
                                            }
                                        )
                                        if (c == '\n') {
                                            break
                                        }
                                    }
                                } else {
                                    for (i in i1 ..< i2) {
                                        a.addLast(
                                            getCharType(text[i]).let {
                                                if (it.code == CharType.ZWNBSP.code && i == 0) {
                                                    CharType.BOM
                                                } else {
                                                    it
                                                }
                                            }
                                        )
                                    }
                                }
                                buildList { // Pair<CharType, IntRange>
                                    var ct0: CharType? = null
                                    var accumulatedOffset = 0
                                    var offset = 0
                                    for (ct in a) {
                                        if (ct == ct0 && (accumulatedOffset + offset) < Int.MAX_VALUE) {
                                            offset += 1
                                        } else {
                                            if (ct0 != null) {
                                                add(Pair(ct0, accumulatedOffset .. (accumulatedOffset + offset)))
                                                accumulatedOffset += 1 + offset
                                                offset = 0
                                            }
                                            ct0 = ct
                                        }
                                    }
                                    if (ct0 != null) {
                                        add(Pair(ct0, accumulatedOffset .. (accumulatedOffset + offset)))
                                    }
                                }.forEach { it ->
                                    val (ct, range) = it
                                    val a = startIndex + range.start
                                    val b = startIndex + range.endInclusive
                                    val s = text.slice(a .. b)
                                    val numberOfUnicodeChars = s.toByteArray(Charsets.UTF_32LE).size / 4
                                    val label = charLabel[ct] ?: "UNKNOWN"
                                    var buttonText by remember { mutableStateOf("") }
                                    buttonText = "$label ($numberOfUnicodeChars)"
                                    var showsDiaLog by remember { mutableStateOf(false) }
                                    if (showsDiaLog) {
                                        when (ct) {
                                            CharType.TEXT -> Dialog(
                                                onDismissRequest = { showsDiaLog = false }
                                            ) {
                                                Card(
                                                    modifier = Modifier
                                                        .width(300.dp)
                                                        .wrapContentHeight()
                                                        .sizeIn(minHeight = 300.dp, maxHeight = 400.dp)
                                                        .verticalScroll(rememberScrollState())
                                                ) {
                                                    Text("TEXT",
                                                        fontSize = 30.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(top = 10.dp, bottom = 0.dp, start = 10.dp, end = 10.dp)
                                                    )
                                                    Text(s,
                                                        fontSize = 30.sp,
                                                        lineHeight = 35.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, start = 20.dp, end = 10.dp)
                                                    )
                                                }
                                            }
                                            CharType.BOM, CharType.SP, CharType.HT, CharType.CR, CharType.LF, CharType.VT, CharType.NBSP, CharType.ZWNBSP -> Dialog(
                                                    onDismissRequest = { showsDiaLog = false }
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.Center,
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.wrapContentSize().verticalScroll(rememberScrollState())
                                                ) {
                                                    when (ct) {
                                                        CharType.BOM -> Button(
                                                            onClick = {
                                                                if (text.isNotEmpty()) {
                                                                    if (text[0].code == CharType.ZWNBSP.code) {
                                                                        value = TextFieldValue(
                                                                            if (text.length > 1) {
                                                                                text.slice(1 ..< text.length)
                                                                            } else {
                                                                                ""
                                                                            },
                                                                            selection = TextRange(
                                                                                max(min(i1 - 1, text.length + 1), 0),
                                                                                max(min(i2 - 1, text.length + 1), 0)
                                                                            )
                                                                        )
                                                                    }
                                                                }
                                                                showsDiaLog = false
                                                            },
                                                            shape = RoundedCornerShape(5.dp),
                                                            modifier = Modifier.width(300.dp)
                                                                .wrapContentHeight()
                                                                .padding(10.dp)
                                                        ) {
                                                            Text(
                                                                "Remove BOM",
                                                                fontSize = 30.sp,
                                                                lineHeight = 35.sp,
                                                                modifier = Modifier.padding(15.dp)
                                                            )
                                                        }
                                                        CharType.HT, CharType.SP, CharType.CR, CharType.LF -> Button(
                                                            onClick = {
                                                                value = TextFieldValue(
                                                                    buildString(text.length) {
                                                                        append(text.slice(0..<a))
                                                                        for (unusedLoopVar in a..b) {
                                                                            append(
                                                                                when (ct) {
                                                                                    CharType.HT -> ' ' // this changes into space; see below
                                                                                    CharType.SP -> '\t'
                                                                                    CharType.CR -> '\n'
                                                                                    else /*LF*/ -> '\r'
                                                                                }
                                                                            )
                                                                        }
                                                                        if (text.length > (b + 1)) {
                                                                            append(text.slice((b + 1) ..< text.length))
                                                                        }
                                                                    },
                                                                    selection = TextRange(
                                                                        max(min(i1, text.length + 1), 0),
                                                                        max(min(i2, text.length + 1), 0)
                                                                    )
                                                                )
                                                                showsDiaLog = false
                                                            },
                                                            shape = RoundedCornerShape(5.dp),
                                                            modifier = Modifier.width(300.dp)
                                                                .wrapContentHeight()
                                                                .padding(10.dp)
                                                        ) {
                                                            Text(
                                                                when (ct) {
                                                                    CharType.HT -> "Change to SP\n(i.e. space(s))"
                                                                    CharType.SP -> "Change to HT\n(i.e. horizontal tab(s))"
                                                                    CharType.CR -> "Change to LF\n(i.e. linefeed(s))"
                                                                    else /*LF*/ -> "Change to CR\n(i.e. carriage return(s))"
                                                                },
                                                                fontSize = 30.sp,
                                                                lineHeight = 35.sp,
                                                                modifier = Modifier.padding(15.dp)
                                                            )
                                                        }
                                                        else -> Unit
                                                    }
                                                    if (ct == CharType.CR || ct == CharType.LF) {
                                                        Button(
                                                            onClick = {
                                                                value = TextFieldValue(
                                                                    buildString(text.length + b - a + 1) {
                                                                        append(text.slice(0..<a))
                                                                        for (unusedLoopVar in a..b) {
                                                                            append("\r\n")
                                                                        }
                                                                        if (text.length > (b + 1)) {
                                                                            append(text.slice((b + 1) ..< text.length))
                                                                        }
                                                                    },
                                                                    selection = TextRange(
                                                                        max(min(i1, text.length + 1), 0),
                                                                        max(min(i2, text.length + 1), 0)
                                                                    )
                                                                )
                                                                showsDiaLog = false
                                                            },
                                                            shape = RoundedCornerShape(5.dp),
                                                            modifier = Modifier.width(300.dp)
                                                                .wrapContentHeight()
                                                                .padding(10.dp)
                                                        ) {
                                                            Text("Change to CRLF\n(i.e., CR followed by LF)",
                                                                fontSize = 30.sp,
                                                                lineHeight = 35.sp,
                                                                modifier = Modifier.padding(15.dp)
                                                            )
                                                        }
                                                    }
                                                    if (ct != CharType.BOM) {
                                                        Button(
                                                            onClick = {
                                                                value = TextFieldValue(
                                                                    buildString(text.length - b + a - 1) {
                                                                        append(text.slice(0..< a))
                                                                        if (text.length > (b + 1)) {
                                                                            append(text.slice((b + 1) ..< text.length))
                                                                        }
                                                                    },
                                                                    selection = TextRange(
                                                                        max(min(i1 + if (ct.code in listOf(CharType.CR.code, CharType.LF.code)) { 1 } else { 0 } - if (b > i2 /* it is i2 here */) { 0 } else { b - a + 1 }, text.length), 0),
                                                                        max(min(i2 + if (ct.code in listOf(CharType.CR.code, CharType.LF.code)) { 1 } else { 0 } - if (b > i2) { 0 } else { b - a + 1 }, text.length), 0)
                                                                    )
                                                                )
                                                                showsDiaLog = false
                                                            },
                                                            shape = RoundedCornerShape(5.dp),
                                                            modifier = Modifier.width(300.dp)
                                                                .wrapContentHeight()
                                                                .padding(10.dp)
                                                        ) {
                                                            Text("Remove",
                                                                fontSize = 30.sp,
                                                                lineHeight = 35.sp,
                                                                modifier = Modifier.padding(15.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            //else -> Unit
                                        }
                                    }
                                    Button(
                                        onClick = { showsDiaLog = true },
                                        shape = RectangleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = color[ct] ?: Color(0xFFA656FF),
                                            contentColor = Color(0xFF000000)
                                        ),
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .padding(2.dp)
                                    ) {
                                        Text(buttonText, fontSize = 30.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private inline fun wordCountAndUnicodeCharacterCount(s: String): Pair<Int, Int> { // word count, unicode character count
    var wordCount = 0
    var unicodeCharCount = s.codePointCount(0, s.length)
    var notInWord = true
    for (i in 0 ..< unicodeCharCount) {
        if (
            Character.isLetterOrDigit(
                s.codePointAt(
                    s.offsetByCodePoints(0, i)
                )
            )
        ) {
            if (notInWord) {
                wordCount += 1
            }
            notInWord = false
        } else {
            notInWord = true
        }
    }
    return Pair(wordCount, unicodeCharCount)
}

private inline fun byteCountAsMeasuredInUTF8(s: String): Int {
    return s.toByteArray(Charsets.UTF_8).size
}
