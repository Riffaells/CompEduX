package ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import components.view.CourseViewComponent
import kotlinx.coroutines.flow.StateFlow
import model.course.CourseDomain
import model.course.CourseStatusDomain
import model.course.CourseVisibilityDomain
import model.course.LocalizedContent
import ui.TechnologyTreeContent
import ui.common.ErrorView
import ui.common.LoadingView
import ui.view.components.CourseEditForm
import ui.view.components.CourseViewDetails
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Компонент для отображения содержимого экрана просмотра курса
 */
@Composable
fun CourseViewContent(
    component: CourseViewComponent,
    modifier: Modifier = Modifier
) {
    val state by component.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Курс", "Схема", "Дерево")

    // Следим за изменением состояния isSaved
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            snackbarHostState.showSnackbar("Курс успешно сохранен")
            component.resetError()
        }
    }

    // Обрабатываем ошибки
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            component.resetError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            state.isCreateMode -> "Создание курса"
                            state.isEditMode -> "Редактирование курса"
                            else -> state.course?.title?.content?.get("ru") ?: "Просмотр курса"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { component.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (!state.isCreateMode && !state.isEditMode) {
                        IconButton(onClick = { component.switchToEditMode() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                    }

                    if (state.isCreateMode || state.isEditMode) {
                        IconButton(
                            onClick = {
                                val course = state.course ?: createEmptyCourse()
                                if (state.isCreateMode) {
                                    component.createCourse(course)
                                } else {
                                    component.updateCourse(course.id, course)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Сохранить")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Показываем FAB только если не в режиме создания или редактирования
            if (!state.isCreateMode && !state.isEditMode) {
                FloatingActionButton(
                    onClick = {
                        component.switchToEditMode()
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Создать курс")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    LoadingView()
                }

                state.error != null -> {
                    ErrorView(error = state.error ?: "Неизвестная ошибка", onRetry = {
                        state.course?.let { component.loadCourse(it.id) }
                    })
                }

                state.isCreateMode || state.isEditMode -> {
                    // Режим создания/редактирования курса
                    CourseEditForm(
                        component = component,
                        initialCourse = state.course ?: createEmptyCourse()
                    )
                }

                else -> {
                    // Режим просмотра курса
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Вкладки
                        TabRow(selectedTabIndex = selectedTabIndex) {
                            tabTitles.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text(title) }
                                )
                            }
                        }

                        // Содержимое вкладки
                        when (selectedTabIndex) {
                            0 -> {
                                // Вкладка "Курс"
                                state.course?.let { course ->
                                    CourseViewDetails(
                                        course = course,
                                        component = component
                                    )
                                }
                            }

                            1 -> {
                            }

                            2 -> {
                                // Вкладка "Дерево технологий"
                                val treeSlot by component.treeSlot.subscribeAsState()

                                Box(modifier = Modifier.fillMaxSize()) {
                                    treeSlot.child?.instance?.let { child ->
                                        when (child) {
                                            is CourseViewComponent.TreeSlotChild.Tree -> {
                                                TechnologyTreeContent(
                                                    component = child.component,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    } ?: run {
                                        // Если дерево технологий не загружено
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            val course = state.course
                                            if (course != null) {
                                                Text(
                                                    "Нажмите кнопку, чтобы загрузить дерево технологий",
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(16.dp)
                                                )
                                                Button(
                                                    onClick = { component.showTechnologyTree() }
                                                ) {
                                                    Text("Загрузить дерево технологий")
                                                }

                                                // Добавляем кнопку для загрузки примера
                                                Spacer(modifier = Modifier.height(16.dp))
                                                OutlinedButton(
                                                    onClick = {
                                                        // Загружаем пример JSON и показываем дерево
                                                        component.showTechnologyTree()

                                                        // Небольшая задержка, чтобы компонент дерева успел инициализироваться
                                                        MainScope().launch {
                                                            delay(500)
                                                            val treeComponent = (component.treeSlot.value.child?.instance as? CourseViewComponent.TreeSlotChild.Tree)?.component

                                                            // Пример дерева технологий в формате JSON
                                                            val exampleJson = """
                                                            {
                                                                "course_id": "${course.id}",
                                                                "data": {
                                                                    "nodes": {
                                                                        "node1": {
                                                                            "id": "node1",
                                                                            "title": {
                                                                                "en": "Introduction to Programming",
                                                                                "ru": "Введение в программирование"
                                                                            },
                                                                            "description": {
                                                                                "en": "Learn the basics of programming and computational thinking",
                                                                                "ru": "Изучите основы программирования и вычислительного мышления"
                                                                            },
                                                                            "position": {
                                                                                "x": 100,
                                                                                "y": 150
                                                                            },
                                                                            "style": "circular",
                                                                            "content_id": "550e8400-e29b-41d4-a716-446655440010",
                                                                            "requirements": [],
                                                                            "type": "TOPIC",
                                                                            "status": "published"
                                                                        },
                                                                        "node2": {
                                                                            "id": "node2",
                                                                            "title": {
                                                                                "en": "Variables and Data Types",
                                                                                "ru": "Переменные и типы данных"
                                                                            },
                                                                            "description": {
                                                                                "en": "Learn about variables, data types, and basic operations",
                                                                                "ru": "Изучите переменные, типы данных и основные операции"
                                                                            },
                                                                            "position": {
                                                                                "x": 250,
                                                                                "y": 100
                                                                            },
                                                                            "style": "hexagon",
                                                                            "content_id": "550e8400-e29b-41d4-a716-446655440011",
                                                                            "requirements": [
                                                                                "node1"
                                                                            ],
                                                                            "type": "SKILL",
                                                                            "status": "published"
                                                                        },
                                                                        "node3": {
                                                                            "id": "node3",
                                                                            "title": {
                                                                                "en": "Control Structures",
                                                                                "ru": "Структуры управления"
                                                                            },
                                                                            "description": {
                                                                                "en": "Learn about conditionals, loops, and flow control",
                                                                                "ru": "Изучите условные операторы, циклы и управление потоком"
                                                                            },
                                                                            "position": {
                                                                                "x": 400,
                                                                                "y": 150
                                                                            },
                                                                            "style": "hexagon",
                                                                            "content_id": "550e8400-e29b-41d4-a716-446655440012",
                                                                            "requirements": [
                                                                                "node2"
                                                                            ],
                                                                            "type": "SKILL",
                                                                            "status": "published"
                                                                        }
                                                                    },
                                                                    "connections": [
                                                                        {
                                                                            "id": "conn1",
                                                                            "from": "node1",
                                                                            "to": "node2",
                                                                            "type": "REQUIRED"
                                                                        },
                                                                        {
                                                                            "id": "conn2",
                                                                            "from": "node2",
                                                                            "to": "node3",
                                                                            "type": "REQUIRED"
                                                                        }
                                                                    ],
                                                                    "metadata": {
                                                                        "defaultLanguage": "ru",
                                                                        "availableLanguages": [
                                                                            "en",
                                                                            "ru"
                                                                        ],
                                                                        "layoutType": "tree",
                                                                        "layoutDirection": "horizontal",
                                                                        "canvasSize": {
                                                                            "width": 1000,
                                                                            "height": 800
                                                                        }
                                                                    }
                                                                },
                                                                "is_published": false,
                                                                "version": 1,
                                                                "id": "${course.id}_tree",
                                                                "created_at": "${java.time.Instant.now()}",
                                                                "updated_at": "${java.time.Instant.now()}"
                                                            }
                                                            """.trimIndent()

                                                            treeComponent?.importTreeFromJson(exampleJson)
                                                        }
                                                    }
                                                ) {
                                                    Text("Создать пример дерева")
                                                }
                                            } else {
                                                Text(
                                                    "Сначала необходимо загрузить курс",
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Отображение сообщения о пустом состоянии
 */
@Composable
private fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Создание пустого курса
 */
private fun createEmptyCourse() = CourseDomain(
    id = "",
    title = LocalizedContent(mapOf("ru" to "")),
    description = LocalizedContent(mapOf("ru" to "")),
    imageUrl = null,
    tags = emptyList(),
    visibility = CourseVisibilityDomain.PUBLIC,
    status = CourseStatusDomain.DRAFT,
    authorId = ""
)

@Composable
fun <T> StateFlow<T>.collectAsState(): State<T> {
    val state = remember { mutableStateOf(value) }
    LaunchedEffect(this) {
        collect { state.value = it }
    }
    return state
}
