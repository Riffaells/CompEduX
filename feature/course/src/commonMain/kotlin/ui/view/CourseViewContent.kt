package ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import components.CourseComponent
import components.view.CourseViewComponent
import ui.view.components.CourseEditForm
import ui.view.components.CourseViewDetails
import ui.view.components.CourseLessonsList
import ui.view.components.CourseSchemaView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseViewContent(
    component: CourseViewComponent,
    parentComponent: CourseComponent? = null
) {
    val state by component.state.collectAsState()
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    
    // Состояние для текущей выбранной вкладки
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    // Список вкладок
    val tabs = listOf("Курс", "Схема", "Уроки")
    
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            showSuccessSnackbar = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.isCreateMode) {
                        Text("Создание курса")
                    } else if (state.isEditMode) {
                        Text("Редактирование курса")
                    } else {
                        Text(
                            text = state.course?.title?.content?.get("ru") 
                                ?: state.course?.title?.content?.get("en") 
                                ?: "Курс"
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { component.onBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    if (!state.isCreateMode && !state.isEditMode && state.course != null) {
                        // Кнопка редактирования в режиме просмотра
                        IconButton(onClick = { component.switchToEditMode() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Добавляем FAB для создания нового курса, если мы не в режиме создания или редактирования
            if (!state.isCreateMode && !state.isEditMode && parentComponent != null) {
                FloatingActionButton(
                    onClick = { 
                        // Используем родительский компонент для навигации к созданию курса
                        parentComponent.navigateToCreateCourse()
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Создать курс")
                }
            }
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
                state.isCreateMode || state.isEditMode -> {
                    // Режим создания или редактирования
                    CourseEditForm(
                        component = component,
                        initialCourse = state.course
                    )
                }
                state.course != null -> {
                    // Режим просмотра с вкладками
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Вкладки
                        TabRow(selectedTabIndex = selectedTabIndex) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text(title) }
                                )
                            }
                        }
                        
                        // Содержимое текущей вкладки
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (selectedTabIndex) {
                                0 -> CourseViewDetails(course = state.course!!)
                                1 -> CourseSchemaView(course = state.course!!)
                                2 -> CourseLessonsList(course = state.course!!)
                            }
                        }
                    }
                }
                else -> {
                    // Пустое состояние
                    Text(
                        text = "Информация о курсе отсутствует",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // Snackbar для отображения успешного сохранения
            if (showSuccessSnackbar) {
                val snackbarHostState = remember { SnackbarHostState() }
                val message = if (state.isCreateMode) "Курс успешно создан" else "Курс успешно обновлен"
                
                LaunchedEffect(snackbarHostState) {
                    snackbarHostState.showSnackbar(message)
                    showSuccessSnackbar = false
                }
                
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}





