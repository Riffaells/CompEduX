package components.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.app.auth.RegisterComponent

@Composable
fun RegisterContent(component: RegisterComponent) {
    val state by component.state.collectAsState()

    // Здесь будет полная реализация экрана регистрации
    // Пока используем заглушку

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Экран регистрации",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { component.onBackClicked() }
        ) {
            Text("Назад")
        }
    }
}
