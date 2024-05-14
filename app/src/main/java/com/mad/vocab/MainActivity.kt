package com.mad.vocab

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
                    val lan = viewModel.lan.collectAsState().value
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
//                                addEditRemove(modifier = Modifier
//                                    .width(200.dp)
//                                    .height(75.dp))
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    items(lan.size) { index ->
                                        productDisplaying(lan[index])
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}

@Composable
fun addEditRemove(modifier: Modifier) {
    var dutch by remember{ mutableStateOf("") }
    var engels by remember { mutableStateOf("") }
    Column {
        TextField(value = "", {})
        TextField(value = "", onValueChange = {})
        Button(onClick = {engels = dutch}) {
            Text(text = "clicked")
        }
    }
}

@Composable
fun productDisplaying(lan: LangObj, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxHeight()
    ){
        Text(text = "${lan.dutch.replaceFirstChar(Char::titlecase)} -> ${lan.engels}",
            modifier = Modifier
                .width(200.dp)
                .height(75.dp)
                .border(
                    width = 2.dp, color = Color.Gray,
                    shape = RoundedCornerShape(10.dp)
                )
                .wrapContentHeight()
                .padding(16.dp),
            textAlign = TextAlign.Center,)
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
