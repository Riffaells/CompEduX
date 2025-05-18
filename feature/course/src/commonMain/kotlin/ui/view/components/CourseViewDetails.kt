package ui.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import model.course.CourseDomain
import model.course.CourseStatusDomain
import model.course.CourseVisibilityDomain
import ui.view.components.ModuleItem
import ui.view.components.ScrollableRow

@Composable
fun CourseViewDetails(course: CourseDomain) {
    // Список доступных языков
    val availableLanguages = listOf(
        "ru" to "Русский",
        "en" to "English",
        "es" to "Español",
        "de" to "Deutsch",
        "fr" to "Français",
        "zh" to "中文",
        "ja" to "日本語"
    )

    // Языки, доступные для этого курса
    val courseLanguages = remember {
        course.title.content.keys.toList().sortedWith(
            compareBy {
                when (it) {
                    "ru" -> 0 // Русский первый
                    "en" -> 1 // Английский второй
                    else -> 2 // Остальные языки
                }
            }
        )
    }

    // Текущий выбранный язык для просмотра
    var selectedLanguage by remember {
        mutableStateOf(
            when {
                "ru" in courseLanguages -> "ru"
                "en" in courseLanguages -> "en"
                courseLanguages.isNotEmpty() -> courseLanguages.first()
                else -> "ru"
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Селектор языка
        if (courseLanguages.size > 1) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp
            ) {
                ScrollableRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    courseLanguages.forEach { langCode ->
                        val langName = availableLanguages.find { it.first == langCode }?.second ?: langCode
                        val isSelected = selectedLanguage == langCode

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLanguage = langCode },
                            label = { Text(langName) }
                        )
                    }
                }
            }
        }

        // Основное содержимое в LazyColumn
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок и описание
            item {
                // Получаем контент для выбранного языка с запасными вариантами
                val title = course.title.content[selectedLanguage]
                    ?: course.title.content["ru"]
                    ?: course.title.content["en"]
                    ?: course.title.content.values.firstOrNull()
                    ?: "Без названия"

                val description = course.description.content[selectedLanguage]
                    ?: course.description.content["ru"]
                    ?: course.description.content["en"]
                    ?: course.description.content.values.firstOrNull()
                    ?: "Без описания"

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // Информация об авторе и тегах
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Автор: ${course.authorName}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (course.tags.isNotEmpty()) {
                                Text(
                                    text = "Теги: ${course.tags.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Информация о видимости и статусе
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Видимость
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = when (course.visibility) {
                                    CourseVisibilityDomain.PRIVATE -> MaterialTheme.colorScheme.errorContainer
                                    CourseVisibilityDomain.UNLISTED -> MaterialTheme.colorScheme.secondaryContainer
                                    CourseVisibilityDomain.PUBLIC -> MaterialTheme.colorScheme.tertiaryContainer
                                },
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (course.visibility) {
                                        CourseVisibilityDomain.PRIVATE -> "Приватный"
                                        CourseVisibilityDomain.UNLISTED -> "По ссылке"
                                        CourseVisibilityDomain.PUBLIC -> "Публичный"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Статус
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = when (course.status) {
                                    CourseStatusDomain.DRAFT -> MaterialTheme.colorScheme.surfaceVariant
                                    CourseStatusDomain.PUBLISHED -> MaterialTheme.colorScheme.primaryContainer
                                    CourseStatusDomain.ARCHIVED -> MaterialTheme.colorScheme.errorContainer
                                    CourseStatusDomain.UNDER_REVIEW -> MaterialTheme.colorScheme.secondaryContainer
                                },
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (course.status) {
                                        CourseStatusDomain.DRAFT -> "Черновик"
                                        CourseStatusDomain.PUBLISHED -> "Опубликован"
                                        CourseStatusDomain.ARCHIVED -> "Архивирован"
                                        CourseStatusDomain.UNDER_REVIEW -> "На проверке"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Заголовок модулей
            item {
                Text(
                    text = "Модули",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            // Модули курса
            if (course.modules.isNotEmpty()) {
                items(course.modules) { module ->
                    ModuleItem(module = module, selectedLanguage = selectedLanguage)
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 1.dp
                    ) {
                        Text(
                            text = "Этот курс не содержит модулей",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        )
                    }
                }
            }

            // Технологическое дерево, если есть
            item {
                if (course.technologyTree != null) {
                    Text(
                        text = "Дерево технологий",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Дерево технологий доступно для этого курса",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { /* Переход к дереву технологий */ }
                            ) {
                                Text("Открыть дерево технологий")
                            }
                        }
                    }
                }
            }
        }
    }
}