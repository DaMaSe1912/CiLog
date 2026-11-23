package com.damay.cilog.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.damay.cilog.ui.components.CilogBottomBar
import com.damay.cilog.ui.navigation.CilogNavGraph

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            CilogBottomBar(navController = navController)
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        CilogNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
