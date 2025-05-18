package ui.lesson

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import components.lesson.LessonViewComponent
import model.course.LessonContentTypeDomain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonViewContent(
    component: LessonViewComponent
) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.lesson?.title?.content?.get("ru") 
                            ?: state.lesson?.title?.content?.get("en") 
                            ?: "Урок"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { component.onBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    // Отображение загрузки
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    // Отображение ошибки
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ошибка: ${state.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(onClick = { component.resetError() }) {
                            Text("Попробовать снова")
                        }
                    }
                }
                state.lesson != null -> {
                    // Отображение урока
                    LessonContent(lesson = state.lesson!!)
                }
                else -> {
                    // Пустое состояние
                    Text(
                        text = "Информация об уроке отсутствует",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
} 