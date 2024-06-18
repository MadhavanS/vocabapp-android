package com.mad.vocab

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.mad.vocab.data.LangRepoImpl
import com.mad.vocab.data.RetroInstance
import com.mad.vocab.data.models.LangObj
import com.mad.vocab.data.presentation.LangViewModel
import com.mad.vocab.ui.theme.VocabAppTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<LangViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T: ViewModel> create(modelClass: Class<T>): T {
                    return LangViewModel(LangRepoImpl(RetroInstance.api)) as T
                }
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VocabAppTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: LangViewModel) {
    val contxt = LocalContext.current
    val showDialog =  remember { mutableStateOf(false) }

    if(showDialog.value)
        CustomDialog(viewModel, value = "", setShowDialog = {
            showDialog.value = it
        }) {
            Log.i("HomePage","HomePage : $it")
        }

    Scaffold (
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showDialog.value = true
                },
            ) {
                Icon(Icons.Filled.Search, "Floating action button.")
            }
        },
        content = { paddingValues ->
            // A surface container using the 'background' color from the theme
            Surface(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
                color = MaterialTheme.colorScheme.background) {
//                    productDisplaying()
                val lan by viewModel.lan.collectAsState()
                val viewModel: LangViewModel = viewModel
                val isReload by viewModel.isRefreshing.collectAsState()
                val swipeRefreshState  = rememberSwipeRefreshState(isRefreshing = isReload)
                val updateStatus by viewModel.updateStatus.collectAsState()

                LaunchedEffect(key1 = viewModel.showErrorToast) {
                    viewModel.showErrorToast.collectLatest {
                            show ->
                        if (show) {
                            Toast.makeText(contxt, "Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                LaunchedEffect(updateStatus) {
                    updateStatus?.let {
                        Toast.makeText(contxt, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearUpdateStatus()
                    }
                }

                if(lan.size === 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    SwipeRefresh(
                        state = swipeRefreshState,
                        onRefresh = viewModel::refreshAndLoad
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth()
                        ) {
                            addEditRemove(viewModel, modifier = Modifier
                                .width(200.dp)
                                .height(75.dp))
                            BodyDisplay(viewModel)
                        }
                    }
                }
            }
        }
    )
}


fun <T> List<T>.chunkedPairs(): List<Pair<T, T?>> {
    val iterator = iterator()
    val result = mutableListOf<Pair<T, T?>>()
    while (iterator.hasNext()) {
        val first = iterator.next()
        val second = if (iterator.hasNext()) iterator.next() else null
        result.add(first to second)
    }
    return result
}


@Composable
fun BodyDisplay(viewModel: LangViewModel) {
    val lan by viewModel.lan.collectAsState()
    val pairedData  = lan.chunkedPairs()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(8.dp)
    ) {
        items(pairedData.size) { index ->
            val pair = pairedData[index]
            ListRow(model1 = pair.first, model2 = pair.second, viewModel)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
@Composable
fun CustomDialog(viewModel: LangViewModel, value: String, setShowDialog: (Boolean) -> Unit, setValue: (String) -> Unit) {
    var searchTxt by remember{ mutableStateOf("") }

    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "",
                            tint = colorResource(android.R.color.darker_gray),
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clickable { setShowDialog(false) }
                        )
                    }
                    OutlinedTextField(value = searchTxt, {searchTxt = it},
                        modifier = Modifier
                            .height(60.dp),
                        label = { Text("search vocab", style = TextStyle(color = Color.Red)) })
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(modifier = Modifier
                        .padding(50.dp, 0.dp, 50.dp, 0.dp)
                        .align(Alignment.CenterHorizontally)) {
                        Button(
                            onClick = {
//                                if (searchTxt.isEmpty()) {
//                                    txtFieldError.value = "Field can not be empty"
//                                    return@Button
//                                }
//                                setValue(txtField.value)
                                setShowDialog(false)
                                viewModel.search(searchTxt.lowercase())
                            },
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.size(width = 100.dp, height = 40.dp)
                        ) {
                            Text(text = "zoek")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListRow(model1: LangObj, model2: LangObj?, viewModel: LangViewModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        productDisplaying(viewModel, model1)
        if (model2 != null) {
            productDisplaying(viewModel, model2)
        }
    }
}

@Composable
fun addEditRemove(viewModel: LangViewModel, modifier: Modifier) {
    val context = LocalContext.current
    var dutch by remember{ mutableStateOf("") }
    var engels by remember { mutableStateOf("") }
    var nlTxt by remember{ mutableStateOf("") }
    var enTxt by remember { mutableStateOf("") }
    val rainbowColors: List<Color> = listOf(Color.Red, Color.Black, Color.DarkGray, Color.Blue, Color.Cyan, Color.Magenta)
    val brush = remember {
        Brush.linearGradient(
            colors = rainbowColors
        )
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(1.dp)
        ) {
            OutlinedTextField(value = dutch, {dutch = it}, textStyle = TextStyle(brush = brush),
                modifier = modifier
                    .height(40.dp),
                label = { Text("dutch", style = TextStyle(color = Color.Red)) })
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(value = engels, {engels = it}, textStyle = TextStyle(brush = brush),
                modifier = modifier
                    .height(40.dp),
                label = { Text("engels", style = TextStyle(color = Color.Red)) })
        }
        Spacer(modifier = Modifier.height(6.dp))
        Button(onClick = {
            nlTxt = dutch.trim()
            enTxt = engels.trim()

            if(nlTxt.isNotEmpty() && enTxt.isNotEmpty()) {
                val postRequest = LangObj(
                    dutch = nlTxt, engels = enTxt
                )

                viewModel.create(postRequest)
                Toast.makeText(context,"Added ${dutch}", Toast.LENGTH_SHORT).show()
                dutch = ""
                engels = ""
            } else {
                Toast.makeText(context,"Please fill both engles and dutch", Toast.LENGTH_SHORT).show()
            }
        },
            modifier = Modifier.size(width = 130.dp, height = 50.dp)) {
            Text(text = "Add Vocab")
        }
    }
}

@Composable
fun productDisplaying(viewModel: LangViewModel, lan: LangObj, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(true)}
    val showDialog =  remember { mutableStateOf(false) }

    if(showDialog.value)
        VocabDialog(viewModel, value = lan, setShowDialog = {
            showDialog.value = it
        }) {
            Log.i("HomePage","HomePage : $it")
        }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxHeight()
    ){
        val txt1 = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Blue)) {
                append(lan.dutch.replaceFirstChar(Char::titlecase))
            }

            withStyle(style = SpanStyle(color = Color.Red)) {
                append(" :: ")
            }

            withStyle(style = SpanStyle(color = Color.Blue)) {
                append(lan.engels)
            }
        }
        ClickableText(
            text = txt1,
            onClick = {
                if(enabled) showDialog.value = true
//                    Toast.makeText(context,"${txt1}", Toast.LENGTH_SHORT).show()
            },
            style = TextStyle(textAlign = TextAlign.Center),
            modifier = Modifier
                .width(175.dp)
                .height(75.dp)
                .border(
                    width = 2.dp, color = Color.Gray,
                    shape = RoundedCornerShape(10.dp)
                )
                .wrapContentHeight()
                .padding(4.dp))
    }
}

@Composable
fun VocabDialog(viewModel: LangViewModel, value: LangObj, setShowDialog: (Boolean) -> Unit, setValue: (String) -> Unit) {
    val context = LocalContext.current
//    val updateStatus by viewModel.updateStatus.collectAsState()

    var dutch by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(value.dutch))
    }
    var engels by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(value.engels))
    }
    var sentence by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(value.sentences?: ""))
    }

//    LaunchedEffect(updateStatus) {
//        updateStatus?.let {
//            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
//            viewModel.clearUpdateStatus()
//        }
//    }

    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "",
                            tint = colorResource(android.R.color.darker_gray),
                            modifier = Modifier
                                .width(20.dp)
                                .height(20.dp)
                                .clickable { setShowDialog(false) }
                        )
                    }
                    OutlinedTextField(value = dutch, {dutch = it},
                        modifier = Modifier
                            .height(60.dp),
                        label = { Text("dutch", style = TextStyle(color = Color.Red)) })
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = engels, {engels = it},
                        modifier = Modifier
                            .height(60.dp),
                        label = { Text("engels", style = TextStyle(color = Color.Red)) })
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(value = sentence, {sentence = it},
                        modifier = Modifier.fillMaxWidth()
                            .height(140.dp).padding(2.dp)
                            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp)),
                        label = { Text("sentence", style = TextStyle(color = Color.Red)) })
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if(dutch.text.isNotEmpty() && dutch.text != value.dutch ||
                                    engels.text.isNotEmpty() && engels.text != value.engels ||
                                    sentence.text != value.sentences
                                ) {
                                    val putRequest = LangObj(
                                        dutch = dutch.text,
                                        engels = engels.text,
                                        sentences = sentence.text,
                                        notes = value.notes
                                    )

                                    viewModel.update(value.dutch, putRequest)
                                } else {
                                    Toast.makeText(context,"Something went wrong!", Toast.LENGTH_SHORT).show()
                                }
                                setShowDialog(false)
                            },
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.size(width = 80.dp, height = 40.dp)
                        ) {
                            Text(text = "save")
                        }
                        Button(
                            onClick = {
                                setShowDialog(false)
                            },
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.size(width = 100.dp, height = 40.dp)
                        ) {
                            Text(text = "cancel")
                        }
                    }
                }
            }
        }
    }
}

//fun productDisplaying(modifier: Modifier = Modifier, viewModel: LangViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
//    val data = viewModel.lan.observeAsState().value
//    if(data !== null) {
//        Text(
//            text = data,
//            modifier = modifier.padding(horizontal = 16.dp)
//        )
//    }
//}

/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VocabAppTheme {
        Greeting("Madhavan")
    }
}*/
