package components.course

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import model.course.*
import utils.getLocalizedText

/**
 * Современный компонент унифицированного контента курса 
 * для просмотра и редактирования содержимого курсов
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernUnifiedCourseContent(
    courseId: String?,
    isLoading: Boolean,
    error: String?,
    course: CourseDomain?,
    modules: List<CourseModuleDomain>,
    isEditMode: Boolean,
    isModified: Boolean,
    onCreateCourse: (CourseDomain) -> Unit,
    onSaveCourse: (CourseDomain) -> Unit,
    onLoadCourse: (String) -> Unit,
    onToggleEditMode: () -> Unit,
    onDiscardChanges: () -> Unit,
    onAddModule: () -> Unit,
    onUpdateModule: (CourseModuleDomain) -> Unit,
    onDeleteModule: (String) -> Unit,
    onMoveModuleUp: (Int) -> Unit,
    onMoveModuleDown: (Int) -> Unit,
    onAddLesson: (String) -> Unit,
    onEditLesson: (String, String) -> Unit,
    onDeleteLesson: (String, String) -> Unit,
    onModuleClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showDeleteCourseDialog by remember { mutableStateOf(false) }
    
    // Состояние редактора курса
    var title by remember(course?.title) { 
        mutableStateOf(course?.title?.let { getLocalizedText(it) } ?: "") 
    }
    var description by remember(course?.description) { 
        mutableStateOf(course?.description?.let { getLocalizedText(it) } ?: "") 
    }
    var isPublished by remember(course?.isPublished) { 
        mutableStateOf(course?.isPublished ?: false) 
    }
    var tags by remember(course?.tags) { 
        mutableStateOf(course?.tags?.toList() ?: emptyList()) 
    }
    
    // Диалог подтверждения выхода из режима редактирования без сохранения
    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text("Несохраненные изменения") },
            text = { Text("У вас есть несохраненные изменения. Вы уверены, что хотите выйти без сохранения?") },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onDiscardChanges()
                        onToggleEditMode()
                    }
                ) {
                    Text("Да, выйти")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showUnsavedChangesDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Диалог подтверждения удаления курса
    if (showDeleteCourseDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCourseDialog = false },
            title = { Text("Удаление курса") },
            text = { Text("Вы уверены, что хотите удалить этот курс? Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = { 
                        // TODO: Добавить функцию удаления курса
                        showDeleteCourseDialog = false 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteCourseDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Эффект для загрузки курса при изменении courseId
    LaunchedEffect(courseId) {
        if (courseId != null) {
            onLoadCourse(courseId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            isLoading -> "Загрузка..."
                            isEditMode -> if (courseId == null) "Создание нового курса" else "Редактирование курса"
                            else -> course?.title?.let { getLocalizedText(it) } ?: "Курс"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditMode && isModified) {
                            showUnsavedChangesDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    if (courseId != null && !isLoading && course != null) {
                        if (isEditMode) {
                            // Режим редактирования
                            IconButton(
                                onClick = { 
                                    val updatedCourse = updateCourse(course, title, description, isPublished, tags)
                                    onSaveCourse(updatedCourse)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Сохранить изменения"
                                )
                            }
                        } else {
                            // Режим просмотра
                            IconButton(onClick = { onToggleEditMode() }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Редактировать курс"
                                )
                            }
                            
                            IconButton(onClick = { showDeleteCourseDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить курс"
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (courseId == null && !isLoading) {
                // Создание нового курса
                ExtendedFloatingActionButton(
                    text = { Text("Создать курс") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = {
                        val newCourse = createNewCourse(title, description, isPublished, tags)
                        onCreateCourse(newCourse)
                    },
                    expanded = true
                )
            } else if (isEditMode && !isLoading && course != null) {
                // Сохранение изменений в режиме редактирования
                ExtendedFloatingActionButton(
                    text = { Text("Сохранить изменения") },
                    icon = { Icon(Icons.Default.Save, contentDescription = null) },
                    onClick = {
                        val updatedCourse = updateCourse(course, title, description, isPublished, tags)
                        onSaveCourse(updatedCourse)
                    },
                    expanded = true
                )
            } else if (!isEditMode && !isLoading && course != null) {
                // Переключение в режим редактирования
                FloatingActionButton(
                    onClick = { onToggleEditMode() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit, 
                        contentDescription = "Редактировать курс"
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> ModernLoadingView()
                error != null -> ModernErrorView(
                    message = error,
                    onRetry = { 
                        if (courseId != null) {
                            onLoadCourse(courseId)
                        }
                    }
                )
                course == null && courseId != null -> {
                    // Курс не найден
                    EmptyStateView(
                        icon = Icons.Outlined.SentimentDissatisfied,
                        title = "Курс не найден",
                        subtitle = "Запрашиваемый курс не существует или был удален",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                isEditMode -> {
                    // Режим редактирования
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TabRow(
                            selectedTabIndex = 0,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(
                                selected = true,
                                onClick = { },
                                text = { Text("Основные данные") }
                            )
                            Tab(
                                selected = false,
                                onClick = { },
                                text = { Text("Модули и уроки") }
                            )
                        }
                        
                        if (courseId == null) {
                            // Создание нового курса
                            ModernCourseEditForm(
                                title = title,
                                description = description,
                                isPublished = isPublished,
                                tags = tags,
                                onTitleChange = { title = it },
                                onDescriptionChange = { description = it },
                                onTogglePublish = { isPublished = !isPublished },
                                onAddTag = { newTag -> tags = tags + newTag },
                                onRemoveTag = { tag -> tags = tags.filter { it != tag } }
                            )
                        } else {
                            // Редактирование существующего курса
                            ModernModuleEditor(
                                modules = modules,
                                onAddModule = onAddModule,
                                onUpdateModule = onUpdateModule,
                                onDeleteModule = onDeleteModule,
                                onMoveModuleUp = onMoveModuleUp,
                                onMoveModuleDown = onMoveModuleDown,
                                onAddLesson = onAddLesson,
                                onEditLesson = onEditLesson,
                                onDeleteLesson = onDeleteLesson
                            )
                        }
                    }
                }
                course != null -> {
                    // Режим просмотра
                    ModernCourseViewDetails(
                        course = course,
                        modules = modules,
                        onModuleClick = onModuleClick
                    )
                }
                else -> {
                    // Пустое состояние (новый курс)
                    EmptyStateView(
                        icon = Icons.Outlined.School,
                        title = "Создание нового курса",
                        subtitle = "Заполните форму, чтобы создать новый курс",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * Компонент для отображения состояния загрузки
 */
@Composable
private fun ModernLoadingView(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Загрузка...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Компонент для отображения ошибки
 */
@Composable
private fun ModernErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(72.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Произошла ошибка",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text("Повторить")
            }
        }
    }
}

/**
 * Создать новый курс из введенных данных
 */
private fun createNewCourse(
    title: String,
    description: String,
    isPublished: Boolean,
    tags: List<String>
): CourseDomain {
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toString()
    
    return CourseDomain(
        id = "",
        title = LocalizedContent.single(title),
        description = LocalizedContent.single(description),
        authorId = "current-user-id", // TODO: Использовать ID текущего пользователя
        authorName = "Current User", // TODO: Использовать имя текущего пользователя
        visibility = CourseVisibilityDomain.PRIVATE,
        isPublished = isPublished,
        tags = tags,
        modules = emptyList(),
        createdAt = now,
        updatedAt = now,
        status = CourseStatusDomain.DRAFT
    )
}

/**
 * Обновить существующий курс
 */
private fun updateCourse(
    course: CourseDomain,
    title: String,
    description: String,
    isPublished: Boolean,
    tags: List<String>
): CourseDomain {

    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toString()

    return course.copy(
        title = LocalizedContent.single(title),
        description = LocalizedContent.single(description),
        isPublished = isPublished,
        tags = tags,
        updatedAt = now,
        status = if (isPublished) CourseStatusDomain.PUBLISHED else CourseStatusDomain.DRAFT
    )
}

/**
 * Современный верхний бар для страницы курса
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernCourseTopBar(
    title: String,
    isEditing: Boolean,
    isDirty: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = { 
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            ) 
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад"
                )
            }
        },
        actions = {
            AnimatedVisibility(
                visible = !isEditing,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать"
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isEditing,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Row {
                    IconButton(
                        onClick = onSaveClick,
                        enabled = isDirty
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Сохранить"
                        )
                    }
                    
                    IconButton(onClick = onCancelClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Отменить"
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        scrollBehavior = scrollBehavior
    )
}

/**
 * Диалог подтверждения отмены изменений
 */
@Composable
private fun ModernDiscardChangesDialog(
    onConfirm: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Ничего не делаем */ },
        icon = { Icon(Icons.Outlined.Warning, contentDescription = null) },
        title = { Text("Несохраненные изменения") },
        text = { 
            Text(
                "У вас есть несохраненные изменения. Хотите сохранить их перед выходом?",
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save, 
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сохранить")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDiscard,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    imageVector = Icons.Default.Close, 
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Отменить изменения", 
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

