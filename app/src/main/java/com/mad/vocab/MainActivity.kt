package com.mad.vocab

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
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
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
//                    productDisplaying()
                    val lan by viewModel.lan.collectAsState()
                    val viewModel: LangViewModel = viewModel
                    val pairedData  = lan.chunkedPairs()
                    val contxt = LocalContext.current
                    val isReload by viewModel.isRefreshing.collectAsState()
                    val swipeRefreshState  = rememberSwipeRefreshState(isRefreshing = isReload)

                    LaunchedEffect(key1 = viewModel.showErrorToast) {
                        viewModel.showErrorToast.collectLatest {
                            show ->
                                if (show) {
                                    Toast.makeText(contxt, "Error", Toast.LENGTH_SHORT).show()
                                }
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
                                    modifier = Modifier.fillMaxHeight()
                                        .fillMaxWidth()
                                ) {
                                    addEditRemove(viewModel, modifier = Modifier
                                        .width(200.dp)
                                        .height(75.dp))
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        contentPadding = PaddingValues(8.dp)
                                    ) {
                                        items(pairedData.size) { index ->
                                            val pair = pairedData[index]
                                            ListRow(model1 = pair.first, model2 = pair.second)
                                            Spacer(modifier = Modifier.height(4.dp))
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
fun ListRow(model1: LangObj, model2: LangObj?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth().padding(2.dp)
    ) {
        productDisplaying(model1)
        if (model2 != null) {
            productDisplaying(model2)
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
                Toast.makeText(context,"Added", Toast.LENGTH_SHORT).show()
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
fun productDisplaying(lan: LangObj, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxHeight()
    ){
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append(lan.dutch.replaceFirstChar(Char::titlecase))
                }

                withStyle(style = SpanStyle(color = Color.Red)) {
                    append(" :: ")
                }

                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append(lan.engels)
                }
            },
            modifier = Modifier
                .width(175.dp)
                .height(75.dp)
                .border(
                    width = 2.dp, color = Color.Gray,
                    shape = RoundedCornerShape(10.dp)
                )
                .wrapContentHeight()
                .padding(4.dp),
            textAlign = TextAlign.Center)
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
