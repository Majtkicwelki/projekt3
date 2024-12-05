package com.example.ognistabaza

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OgnistaBazka()
        }
    }
}

@Composable
fun OgnistaBazka() {
    val firestore = FirebaseFirestore.getInstance()
    val collectionName = "Bazka"

    var inputText by remember { mutableStateOf("") }
    var displayText by remember { mutableStateOf("Odczytane dane:\n") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Wpisz dane", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                saveTextToFirestore(firestore, collectionName, inputText) {
                    inputText = ""
                    fetchTextFromFirestore(firestore, collectionName) { fetchedText ->
                        displayText = "Odczytane dane:\n$fetchedText"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Wyślij do Firebase",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = displayText,
            textAlign = TextAlign.Start,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }


    LaunchedEffect(Unit) {
        fetchTextFromFirestore(firestore, collectionName) { fetchedText ->
            displayText = "Odczytane dane:\n$fetchedText"
        }
    }
}


fun saveTextToFirestore(
    firestore: FirebaseFirestore,
    collectionName: String,
    input: String,
    onSuccess: () -> Unit
) {
    if (input.isEmpty()) {
        return
    }

    val documentData = hashMapOf("content" to input)

    firestore.collection(collectionName).add(documentData)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e ->
            println("Błąd : ${e.message}")
        }
}

fun fetchTextFromFirestore(
    firestore: FirebaseFirestore,
    collectionName: String,
    onResult: (String) -> Unit
) {
    firestore.collection(collectionName).get()
        .addOnSuccessListener { documents ->
            val text = documents.joinToString(separator = "\n") { document ->
                document.getString("content") ?: ""
            }
            onResult(text)
        }
        .addOnFailureListener { e ->
            println("Błąd : ${e.message}")
        }
}
