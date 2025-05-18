package ui.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import model.course.CourseDomain
import model.course.CourseLessonDomain
import model.course.CourseModuleDomain

@Composable
fun CourseLessonsList(course: CourseDomain) {
    val modules = course.modules.sortedBy { it.order }
    
    if (modules.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "В курсе пока нет уроков",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Список уроков",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Отображаем все модули и уроки в них
            modules.forEach { module ->
                item {
                    ModuleHeader(module)
                }
                
                val lessons = module.lessons.sortedBy { it.order }
                if (lessons.isNotEmpty()) {
                    items(lessons) { lesson ->
                        LessonItem(lesson)
                    }
                } else {
                    item {
                        Text(
                            text = "В этом модуле пока нет уроков",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }
                }
                
                // Разделитель между модулями
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ModuleHeader(module: CourseModuleDomain) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = module.title.getPreferredString() ?: "Модуль ${module.order}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${module.lessons.size} ${getLessonsCountText(module.lessons.size)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonItem(lesson: CourseLessonDomain) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 0.dp, top = 4.dp, bottom = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { /* Переход к уроку */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title.getPreferredString() ?: "Урок ${lesson.order}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                if (lesson.description != null) {
                    val description = lesson.description?.getPreferredString()
                    if (!description.isNullOrBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Открыть урок",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Вспомогательная функция для правильного склонения слова "урок"
private fun getLessonsCountText(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "урок"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "урока"
        else -> "уроков"
    }
} 