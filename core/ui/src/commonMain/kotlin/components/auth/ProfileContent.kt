package components.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.app.auth.ProfileComponent

@Composable
fun ProfileContent(component: ProfileComponent) {
    val state by component.state.collectAsState()

    // Здесь будет полная реализация экрана профиля
    // Пока используем заглушку

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Профиль пользователя",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { component.onLogoutClicked() }
        ) {
            Text("Выйти")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { component.onBackClicked() }
        ) {
            Text("Назад")
        }
    }
}
