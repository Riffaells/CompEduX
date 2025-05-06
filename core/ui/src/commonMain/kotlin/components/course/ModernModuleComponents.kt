package components.course

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import model.course.CourseModuleDomain
import model.course.CourseLessonDomain
import model.course.LessonContentTypeDomain
import model.course.LocalizedContent
import utils.getLocalizedText

/**
 * Компонент для редактирования модулей курса
 */
@Composable
fun ModernModuleEditor(
    modules: List<CourseModuleDomain>,
    onAddModule: () -> Unit,
    onUpdateModule: (CourseModuleDomain) -> Unit,
    onDeleteModule: (String) -> Unit,
    onMoveModuleUp: (Int) -> Unit,
    onMoveModuleDown: (Int) -> Unit,
    onAddLesson: (String) -> Unit,
    onEditLesson: (String, String) -> Unit,
    onDeleteLesson: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Модули курса",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = onAddModule,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text("Добавить модуль")
                }
            }
        }
        
        // Список модулей
        if (modules.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Outlined.FolderOpen,
                    title = "Нет модулей",
                    subtitle = "Добавьте модули для вашего курса",
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            itemsIndexed(modules) { index, module ->
                ModuleEditorItem(
                    module = module,
                    isFirstModule = index == 0,
                    isLastModule = index == modules.size - 1,
                    onUpdateModule = onUpdateModule,
                    onDeleteModule = { onDeleteModule(module.id) },
                    onMoveUp = { onMoveModuleUp(index) },
                    onMoveDown = { onMoveModuleDown(index) },
                    onAddLesson = { onAddLesson(module.id) },
                    onEditLesson = { lessonId -> onEditLesson(module.id, lessonId) },
                    onDeleteLesson = { lessonId -> onDeleteLesson(module.id, lessonId) }
                )
            }
        }
    }
}

/**
 * Элемент редактора модуля
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleEditorItem(
    module: CourseModuleDomain,
    isFirstModule: Boolean,
    isLastModule: Boolean,
    onUpdateModule: (CourseModuleDomain) -> Unit,
    onDeleteModule: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onAddLesson: () -> Unit,
    onEditLesson: (String) -> Unit,
    onDeleteLesson: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(if (expanded) 180f else 0f, label = "")
    
    var title by remember(module.title) {
        mutableStateOf(getLocalizedText(module.title))
    }
    var description by remember(module.description) {
        mutableStateOf(getLocalizedText(module.description))
    }
    
    val focusRequester = remember { FocusRequester() }
    
    // Диалог подтверждения удаления модуля
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление модуля") },
            text = { Text("Вы уверены, что хотите удалить модуль «${getLocalizedText(module.title)}»?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteModule()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Заголовок модуля
            if (isEditMode) {
                // Режим редактирования
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Редактирование модуля",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Название модуля") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Описание модуля") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { isEditMode = false }
                        ) {
                            Text("Отмена")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                val updatedModule = module.copy(
                                    title = LocalizedContent.single(title),
                                    description = LocalizedContent.single(description)
                                )
                                onUpdateModule(updatedModule)
                                isEditMode = false
                            }
                        ) {
                            Text("Сохранить")
                        }
                    }
                }
                
                LaunchedEffect(isEditMode) {
                    if (isEditMode) {
                        focusRequester.requestFocus()
                    }
                }
            } else {
                // Режим просмотра
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(16.dp)
                ) {
                    // Номер модуля
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = "${module.order + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Информация о модуле
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getLocalizedText(module.title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = getLocalizedText(module.description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${module.lessons.size} уроков",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Кнопки управления
                    IconButton(onClick = { isEditMode = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать модуль"
                        )
                    }
                    
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Свернуть" else "Развернуть",
                            modifier = Modifier.rotate(rotationState)
                        )
                    }
                }
            }
            
            // Раскрывающаяся панель с уроками
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Divider()
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Кнопки действий для модуля
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onMoveUp,
                            enabled = !isFirstModule,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Переместить вверх"
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text("Вверх")
                        }
                        
                        OutlinedButton(
                            onClick = onMoveDown,
                            enabled = !isLastModule,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Переместить вниз"
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text("Вниз")
                        }
                        
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить модуль"
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text("Удалить")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Заголовок списка уроков
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Уроки модуля",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Button(
                            onClick = onAddLesson
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Добавить урок"
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text("Добавить урок")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Список уроков
                    if (module.lessons.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
                            Text(
                                text = "В этом модуле пока нет уроков",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            module.lessons.forEach { lesson ->
                                LessonListItem(
                                    lesson = lesson,
                                    onEdit = { onEditLesson(lesson.id) },
                                    onDelete = { onDeleteLesson(lesson.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Элемент списка уроков для редактирования
 */
@Composable
private fun LessonListItem(
    lesson: CourseLessonDomain,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление урока") },
            text = { Text("Вы уверены, что хотите удалить урок «${getLocalizedText(lesson.title)}»?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Иконка типа урока
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(getLessonTypeColor(lesson.contentType))
            ) {
                Icon(
                    imageVector = getLessonTypeIcon(lesson.contentType),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Информация об уроке
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getLocalizedText(lesson.title),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = getLessonTypeText(lesson.contentType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Кнопки управления
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Редактировать урок",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить урок",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Получить иконку для типа урока
 */
@Composable
private fun getLessonTypeIcon(type: LessonContentTypeDomain): ImageVector {
    return when (type) {
        LessonContentTypeDomain.VIDEO -> Icons.Default.PlayArrow
        LessonContentTypeDomain.ARTICLE -> Icons.Default.Article
        LessonContentTypeDomain.QUIZ -> Icons.Default.Quiz
        LessonContentTypeDomain.CODE_EXERCISE -> Icons.Default.Code
        LessonContentTypeDomain.INTERACTIVE -> Icons.Default.TouchApp
    }
}

/**
 * Получить цвет для типа урока
 */
@Composable
private fun getLessonTypeColor(type: LessonContentTypeDomain): Color {
    return when (type) {
        LessonContentTypeDomain.VIDEO -> MaterialTheme.colorScheme.primary
        LessonContentTypeDomain.ARTICLE -> MaterialTheme.colorScheme.secondary
        LessonContentTypeDomain.QUIZ -> MaterialTheme.colorScheme.tertiary
        LessonContentTypeDomain.CODE_EXERCISE -> MaterialTheme.colorScheme.error
        LessonContentTypeDomain.INTERACTIVE -> MaterialTheme.colorScheme.surfaceTint
    }
}

/**
 * Получить текстовое описание типа урока
 */
private fun getLessonTypeText(type: LessonContentTypeDomain): String {
    return when (type) {
        LessonContentTypeDomain.VIDEO -> "Видеоурок"
        LessonContentTypeDomain.ARTICLE -> "Текстовый урок"
        LessonContentTypeDomain.QUIZ -> "Тест"
        LessonContentTypeDomain.CODE_EXERCISE -> "Практическое задание"
        LessonContentTypeDomain.INTERACTIVE -> "Интерактивное задание"
    }
} 