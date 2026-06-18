package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.local.DatingDatabase
import com.example.data.repository.DatingRepository
import com.example.ui.screens.DateVibeAppContent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DatingViewModel
import com.example.ui.viewmodel.DatingViewModelFactory

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup local Room dating persistence database
        val database = DatingDatabase.getDatabase(this)
        val repository = DatingRepository(database.datingDao())
        
        // Instantiate the main Dating viewmodel
        val viewModel: DatingViewModel by viewModels {
            DatingViewModelFactory(application, repository)
        }
        
        // Edge-to-edge drawing
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DateVibeAppContent(viewModel = viewModel)
                }
            }
        }
    }
}
