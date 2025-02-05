package com.superacm.demo.home.video.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.superacm.demo.home.video.data.DateViewModel
import com.superacm.demo.home.video.util.ImageLoadUtil


@Composable
fun DateSelectionPage(viewModel: DateViewModel) {
    LaunchedEffect(viewModel) {
        viewModel.refreshItemList()
    }
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            // Date selection section
            //        DateSelectionSection(viewModel)
            //        Spacer(modifier = Modifier.height(16.dp))
            DatePickerDocked {
                viewModel.selectedDate.value = convertMillisToDate(it)
                viewModel.refreshItemList()
            }
            Spacer(modifier = Modifier.height(16.dp))

            // List section
            ItemListSection(viewModel)
        }

        if(viewModel.dataLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun ItemListSection(viewModel: DateViewModel) {
    // Display the list of items based on the current selected date
    val ctx = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(viewModel.itemList.value) { item ->
            EventListItem(index = 0, editableMsgBean = item, onClick = {
                viewModel.handleClick(ctx, item)
            })
        }
    }
}

@Composable
fun EventImage(url: String) {
    AsyncImage(model = url,
        imageLoader = ImageLoadUtil.imageLoader,
        contentDescription = "",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds,
        onError = {
        })
}

// Preview
@Preview(showBackground = true)
@Composable
fun PreviewDateSelectionPage() {
    DateSelectionPage(DateViewModel(null))
}
