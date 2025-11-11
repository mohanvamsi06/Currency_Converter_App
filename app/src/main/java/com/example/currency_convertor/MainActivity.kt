package com.example.currency_convertor

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.currency_convertor.network.RetrofitClient
import com.example.currency_convertor.ui.theme.Currency_ConvertorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Currency_ConvertorTheme {
                App()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.currency_converter_title)) }
            )
        },
        /*bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_converter), contentDescription = "Converter")
                    Text("Converter")
                }
            }
        }*/
    ) { innerPadding ->
        SelectionScreen(modifier = Modifier.padding(innerPadding))
    }
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_SHORT
    ).show()
}

@Composable
fun SelectionScreen(modifier: Modifier = Modifier) {
    var amount by remember { mutableStateOf("1000") }
    var sourceCurrency by remember { mutableStateOf("") }
    var targetCurrency by remember { mutableStateOf("") }
    var conversionResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val usdCurrency = stringResource(R.string.usd_currency_name)
    val eurCurrency = stringResource(R.string.eur_currency_name)

    LaunchedEffect(Unit) {
        sourceCurrency = usdCurrency
        targetCurrency = eurCurrency
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        CurrencyDropdown(
            label = stringResource(R.string.from_label),
            selectedOption = sourceCurrency,
            onOptionSelected = { sourceCurrency = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        IconButton(
            onClick = {
                val tmp = sourceCurrency
                sourceCurrency = targetCurrency
                targetCurrency = tmp
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = stringResource(R.string.exchange_button_content_description),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CurrencyDropdown(
            label = stringResource(R.string.to_label),
            selectedOption = targetCurrency,
            onOptionSelected = { targetCurrency = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(stringResource(R.string.amount_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                val amountToConvert = amount.replace(",", "").toDoubleOrNull()
                if (amountToConvert == null) {
                    showToast(context, context.getString(R.string.invalid_amount_toast))
                    return@Button
                }

                coroutineScope.launch {
                    isLoading = true
                    conversionResult = null
                    try {
                        val response = RetrofitClient.instance.getConversionRate(
                            baseCode = sourceCurrency.split(" ")[0],
                            targetCode = targetCurrency.split(" ")[0]
                        )
                        val convertedAmount = amountToConvert * response.conversionRate
                        conversionResult = context.getString(
                            R.string.conversion_result_format,
                            amountToConvert,
                            sourceCurrency,
                            convertedAmount,
                            targetCurrency
                        )
                    } catch (e: Exception) {
                        val errorMessage = context.getString(R.string.conversion_failed_toast, e.message)
                        showToast(context, errorMessage)
                        conversionResult = errorMessage
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
        ) {
            Text(stringResource(R.string.convert_button_text))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            conversionResult?.let {
                Text(text = it)
            }
        }
    }
}

@Composable
fun CurrencyDropdown(
    label: String,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        stringResource(R.string.usd_currency_name),
        stringResource(R.string.eur_currency_name),
        stringResource(R.string.inr_currency_name),
        stringResource(R.string.jpy_currency_name),
        stringResource(R.string.cad_currency_name)
    )

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                        showToast(context, context.getString(R.string.selected_toast, option))
                    }
                )
            }
        }
    }
}
