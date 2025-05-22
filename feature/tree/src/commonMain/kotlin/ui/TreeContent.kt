package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.TechnologyTreeComponent
import component.TechnologyTreeStore
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import model.tree.TechnologyTreeDomain
import ui.tree.NodeLabels
import ui.tree.TreeCanvas

/**
 * Основной компонент для отображения и взаимодействия с деревом технологий
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnologyTreeContent(
    modifier: Modifier = Modifier,
    component: TechnologyTreeComponent
) {
    val scope = rememberCoroutineScope()
    val state by component.state.collectAsState()

    // Состояние UI
    var jsonText by remember { mutableStateOf("") }
    var showJsonImport by remember { mutableStateOf(false) }
    var isJsonValid by remember { mutableStateOf(true) }
    var showZoomControls by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }

    // Инициализируем с центрированием
    var panOffset by remember { mutableStateOf(Offset(300f, 200f)) }
    var zoomLevel by remember { mutableStateOf(1f) }

    // Состояние для отображения текущего JSON дерева
    var showCurrentTreeJson by remember { mutableStateOf(false) }
    var currentTreeJson by remember { mutableStateOf("") }

    // Пульсация для выбранного узла
    val pulseScale by animateFloatAsState(
        targetValue = if (state.selectedNodeId != null) 1.05f else 1f,
        animationSpec = tween(
            durationMillis = 800,
            easing = LinearEasing
        ),
        label = "pulseAnimation"
    )

    // Инициализация при запуске
    LaunchedEffect(Unit) {
        component.onEvent(TechnologyTreeStore.Intent.Init)
    }

    // Обновление JSON представления дерева при изменении состояния
    LaunchedEffect(state.technologyTree) {
        if (state.technologyTree != null) {
            try {
                val jsonFormatter = Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
                currentTreeJson = jsonFormatter.encodeToString(TechnologyTreeDomain.serializer(), state.technologyTree!!)
            } catch (e: Exception) {
                println("Ошибка сериализации дерева в JSON: ${e.message}")
            }
        }
    }

    // Вывод отладочной информации о состоянии
    LaunchedEffect(state) {
        if (state.technologyTree != null) {
            println("TreeContent: Дерево загружено, ${state.technologyTree?.nodes?.size ?: 0} узлов, ${state.technologyTree?.connections?.size ?: 0} соединений")
            state.technologyTree?.nodes?.forEachIndexed { index, node ->
                println("TreeContent узел #$index: ${node.id}, позиция=(${node.position.x}, ${node.position.y}), тип=${node.type}")
            }
        } else if (state.error != null) {
            println("TreeContent: Ошибка загрузки: ${state.error}")
        }
    }

    // Автоматический импорт JSON, если он предоставлен
    LaunchedEffect(jsonText) {
        if (jsonText.isNotBlank() && isJsonValid && jsonText.length > 20) {
            delay(500) // Даем небольшую задержку для лучшего UX
            try {
                component.importTreeFromJson(jsonText)
            } catch (e: Exception) {
                println("Error auto-importing JSON: ${e.message}")
                isJsonValid = false
            }
        }
    }

    // Показываем зум-контроллеры на некоторое время и скрываем
    LaunchedEffect(showZoomControls) {
        if (showZoomControls) {
            delay(5000)
            showZoomControls = false
        }
    }

    // Добавляем диалог для отображения текущего JSON дерева
    if (showCurrentTreeJson && currentTreeJson.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { showCurrentTreeJson = false },
            title = { Text("JSON представление дерева") },
            text = {
                Column {
                    Text("Это JSON-представление текущего дерева, которое можно скопировать:")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(rememberScrollState())
                            .background(Color(0xFF1E1E1E))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = currentTreeJson,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCurrentTreeJson = false }) {
                    Text("Закрыть")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Здесь бы был код для копирования в буфер обмена,
                        // но это зависит от платформы
                        showCurrentTreeJson = false
                    }
                ) {
                    Text("Копировать")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(18, 18, 18))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Заголовок и действия
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Визуализация дерева курса",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Row {
                    // Кнопка просмотра JSON дерева
                    if (state.technologyTree != null) {
                        Button(
                            onClick = { showCurrentTreeJson = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = "Показать JSON")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Показать JSON")
                        }
                    }

                    // Кнопка обновления дерева
                    if (state.technologyTree != null) {
                        Button(
                            onClick = {
                                component.onEvent(TechnologyTreeStore.Intent.Init)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Обновить")
                        }
                    }

                    // Кнопка для показа/скрытия импорта JSON
                    TextButton(
                        onClick = { showJsonImport = !showJsonImport },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (showJsonImport) MaterialTheme.colorScheme.primary else Color.White
                        )
                    ) {
                        Icon(
                            if (showJsonImport) Icons.Default.CodeOff else Icons.Default.Code,
                            contentDescription = "JSON"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showJsonImport) "Скрыть JSON" else "Показать JSON")
                    }
                }
            }

            // Панель импорта JSON
            AnimatedVisibility(
                visible = showJsonImport,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .background(
                            color = Color(30, 30, 30),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Импорт дерева технологий из JSON",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = jsonText,
                        onValueChange = {
                            jsonText = it
                            isJsonValid = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp, max = 300.dp),
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        placeholder = { Text("Вставьте JSON дерева технологий здесь...", color = Color.Gray) },
                        isError = !isJsonValid,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        )
                    )

                    if (!isJsonValid) {
                        Text(
                            text = "Неверный формат JSON",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                jsonText = ""
                                isJsonValid = true
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.Gray
                            )
                        ) {
                            Text("Очистить")
                        }

                        // Кнопка для вставки примера JSON
                        TextButton(
                            onClick = {
                                jsonText = """
                                {
                                    "course_id": "8fc512fc-844f-490e-a2c1-be69b1173dda",
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
                                                "type": "article",
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
                                                "type": "article",
                                                "status": "published"
                                            }
                                        },
                                        "connections": [
                                            {
                                                "id": "conn1",
                                                "from": "node1",
                                                "to": "node2",
                                                "type": "required"
                                            }
                                        ],
                                        "metadata": {
                                            "defaultLanguage": "en",
                                            "availableLanguages": [
                                                "en",
                                                "ru"
                                            ],
                                            "layoutType": "tree",
                                            "layoutDirection": "horizontal",
                                            "canvasSize": {
                                                "width": 800,
                                                "height": 600
                                            }
                                        }
                                    },
                                    "is_published": false,
                                    "version": 1,
                                    "id": "108f33e1-7d50-4825-9a21-33406de816fe",
                                    "created_at": "2025-05-22T10:55:46.008565Z",
                                    "updated_at": "2025-05-22T10:55:46.008569Z"
                                }
                                """.trimIndent()
                                isJsonValid = true
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Вставить пример")
                        }

                        Button(
                            onClick = {
                                try {
                                    if (jsonText.isNotBlank()) {
                                        component.importTreeFromJson(jsonText)
                                    }
                                } catch (e: Exception) {
                                    println("[ERROR] JSON parsing error: ${e.message}")
                                    isJsonValid = false
                                }
                            },
                            enabled = jsonText.isNotBlank(),
                            modifier = Modifier.padding(start = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = Color(60, 60, 60)
                            )
                        ) {
                            Text("Импортировать JSON")
                        }
                    }
                }
            }

            // Индикатор загрузки, ошибка или дерево
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (showJsonImport) 8.dp else 16.dp)
            ) {
                when {
                    // Показываем индикатор загрузки
                    state.isLoading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Загрузка дерева технологий...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }

                    // Показываем ошибку
                    state.error != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .background(
                                    color = Color(40, 30, 30),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Ошибка",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = state.error ?: "Неизвестная ошибка",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                            Button(
                                onClick = { component.onEvent(TechnologyTreeStore.Intent.Init) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Повторить")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Повторить загрузку")
                            }
                        }
                    }

                    // Дерево не загружено
                    state.technologyTree == null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .background(
                                    color = Color(30, 30, 40),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Инфо",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Дерево технологий не загружено",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                            Button(
                                onClick = { component.onEvent(TechnologyTreeStore.Intent.Init) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Загрузить")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Загрузить дерево")
                            }
                        }
                    }

                    // Отображаем дерево
                    else -> {
                        // Контейнер для дерева с обводкой
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 1.dp,
                                    color = Color(40, 40, 40),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            // Основной канвас дерева
                            state.technologyTree?.let { tree ->
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Отрисовка дерева
                                    TreeCanvas(
                                        treeData = tree,
                                        selectedNodeId = state.selectedNodeId,
                                        panOffset = panOffset,
                                        pulseScale = pulseScale,
                                        showGrid = showGrid,
                                        onNodeClick = { nodeId ->
                                            component.onEvent(TechnologyTreeStore.Intent.NodeClicked(nodeId))
                                        }
                                    )

                                    // Отрисовка меток узлов поверх канваса
                                    NodeLabels(
                                        treeData = tree,
                                        selectedNodeId = state.selectedNodeId,
                                        panOffset = panOffset
                                    )

                                    // Панель управления масштабом и перемещением
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(16.dp)
                                            .alpha(if (showZoomControls) 1f else 0.2f)
                                    ) {
                                        FloatingActionButton(
                                            onClick = {
                                                zoomLevel = (zoomLevel * 1.2f).coerceAtMost(3f)
                                                showZoomControls = true
                                            },
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Default.Add, "Увеличить", modifier = Modifier.size(20.dp))
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        FloatingActionButton(
                                            onClick = {
                                                zoomLevel = (zoomLevel * 0.8f).coerceAtLeast(0.5f)
                                                showZoomControls = true
                                            },
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, "Уменьшить", modifier = Modifier.size(20.dp))
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        FloatingActionButton(
                                            onClick = {
                                                panOffset = Offset.Zero
                                                zoomLevel = 1f
                                                showZoomControls = true
                                            },
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, "Сбросить", modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    // Информация о выбранном узле
                                    state.selectedNodeId?.let { selectedId ->
                                        val selectedNode = tree.nodes.find { it.id == selectedId }
                                        selectedNode?.let { node ->
                                            Card(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(16.dp)
                                                    .width(250.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(30, 30, 30, 230)
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Text(
                                                        text = node.title.content["ru"] ?: "Без названия",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = Color.White
                                                    )

                                                    Divider(
                                                        modifier = Modifier.padding(vertical = 8.dp),
                                                        color = Color.Gray.copy(alpha = 0.5f)
                                                    )

                                                    Text(
                                                        text = node.description.content["ru"] ?: "Без описания",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White.copy(alpha = 0.8f)
                                                    )

                                                    Divider(
                                                        modifier = Modifier.padding(vertical = 8.dp),
                                                        color = Color.Gray.copy(alpha = 0.5f)
                                                    )

                                                    Text(
                                                        text = "Тип: ${node.type.name}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White.copy(alpha = 0.6f)
                                                    )

                                                    if (node.requirements.isNotEmpty()) {
                                                        Text(
                                                            text = "Требуется: ${node.requirements.joinToString(", ")}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.White.copy(alpha = 0.6f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Инструкции для пользователей
                                    Card(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                            .alpha(0.8f),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(30, 30, 30, 200)
                                        )
                                    ) {
                                        Text(
                                            text = "Используйте мышь для перемещения и клика на узлах",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    // Кнопка быстрого сброса настроек отображения
                                    FloatingActionButton(
                                        onClick = {
                                            // Сбрасываем смещение панорамирования к начальному значению с центрированием
                                            panOffset = Offset(300f, 200f)
                                            zoomLevel = 1f
                                            showZoomControls = true
                                        },
                                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
                                        contentColor = MaterialTheme.colorScheme.onTertiary,
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Home,
                                            contentDescription = "Сбросить панорамирование",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопки для управления панорамированием
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D2D2D)
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Навигация по холсту",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Кнопка сброса позиции
                        Button(
                            onClick = {
                                panOffset = Offset(300f, 200f)
                                zoomLevel = 1f
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Сбросить")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Сбросить")
                        }

                        // Кнопка центрирования на первом узле
                        Button(
                            onClick = {
                                val firstNode = state.technologyTree?.nodes?.firstOrNull()
                                if (firstNode != null) {
                                    // Центрируем на первом узле с учетом смещения
                                    // Используем примерные размеры экрана
                                    panOffset = Offset(
                                        400f - firstNode.position.x,
                                        300f - firstNode.position.y
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            enabled = state.technologyTree?.nodes?.isNotEmpty() == true
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Центрировать")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("На первый узел")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Переключатель отображения сетки
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Показывать сетку",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )

                        Switch(
                            checked = showGrid,
                            onCheckedChange = { showGrid = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Масштаб: ${(zoomLevel * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Slider(
                        value = zoomLevel,
                        onValueChange = { zoomLevel = it },
                        valueRange = 0.5f..2f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Отображаем текущую информацию о панорамировании
                    Text(
                        "Смещение: X=${panOffset.x.toInt()}, Y=${panOffset.y.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Секция отладочной информации
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D2D2D)
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Отладочная информация",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Статистика по дереву
                    val tree = state.technologyTree
                    if (tree != null) {
                        Text(
                            "ID дерева: ${tree.id}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            "Версия: ${tree.version}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            "Узлов: ${tree.nodes.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            "Соединений: ${tree.connections.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )

                        // Информация о выбранном узле
                        val selectedNode = state.selectedNodeId?.let { id ->
                            tree.nodes.find { it.id == id }
                        }

                        if (selectedNode != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Выбранный узел: ${selectedNode.id}",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                "Заголовок: ${selectedNode.title.content}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Text(
                                "Позиция: (${selectedNode.position.x}, ${selectedNode.position.y})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    } else {
                        Text(
                            "Дерево не загружено",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}
