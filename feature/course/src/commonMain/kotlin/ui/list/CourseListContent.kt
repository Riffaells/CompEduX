package ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.CourseComponent
import components.list.CourseListComponent
import ui.list.components.CourseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListContent(
    component: CourseListComponent,
    parentComponent: CourseComponent? = null
) {
    val state by component.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        component.filterCourses(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Курсы") },
                navigationIcon = {
                    IconButton(onClick = { component.onBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    // Кнопка обновления списка
                    IconButton(
                        onClick = { component.refreshCourses() },
                        enabled = !state.isRefreshing && !state.isLoading
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Обновить"
                            )
                        }
                    }
                    
                    // Кнопка создания нового курса в верхней панели
                    if (parentComponent != null) {
                        IconButton(onClick = { parentComponent.navigateToCreateCourse() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Создать курс"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Плавающая кнопка для создания курса
            if (parentComponent != null) {
                FloatingActionButton(
                    onClick = { parentComponent.navigateToCreateCourse() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Создать курс")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Поисковая строка
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { component.filterCourses(it) },
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Поиск курсов") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                trailingIcon = {},
                content = {}
            )

            // Состояние загрузки при первичной загрузке
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                // Отображение ошибки
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Ошибка: ${state.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(onClick = { component.loadCourses() }) {
                            Text("Повторить")
                        }
                    }
                }
            } else if (state.filteredCourses.isEmpty()) {
                // Пустой список
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Нет доступных курсов" else "По запросу ничего не найдено",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (searchQuery.isBlank() && parentComponent != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { parentComponent.navigateToCreateCourse() }
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Создать новый курс")
                            }
                        }
                    }
                }
            } else {
                // Список курсов
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredCourses) { course ->
                        CourseItem(
                            course = course,
                            onClick = { component.selectCourse(course.id) }
                        )
                    }
                }
            }
        }
    }
}
