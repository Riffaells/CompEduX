package ui.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import components.view.CourseViewComponent
import model.course.CourseDomain
import model.course.CourseStatusDomain
import model.course.CourseVisibilityDomain
import model.course.LocalizedContent
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditForm(
    component: CourseViewComponent,
    initialCourse: CourseDomain? = null
) {
    var course by remember { mutableStateOf(initialCourse ?: createEmptyCourse()) }
    val scrollState = rememberScrollState()

    // Определяем доступные языки
    val availableLanguages = remember {
        listOf(
            "ru" to "Русский",
            "en" to "English"
        )
    }

    // Выбранный язык для редактирования
    var selectedLanguageIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Табы для выбора языка
        TabRow(selectedTabIndex = selectedLanguageIndex) {
            availableLanguages.forEachIndexed { index, (code, name) ->
                Tab(
                    selected = selectedLanguageIndex == index,
                    onClick = { selectedLanguageIndex = index },
                    text = { Text(name) },
                    icon = {
                        if (index == selectedLanguageIndex) {
                            Icon(Icons.Default.Language, contentDescription = null)
                        }
                    }
                )
            }
        }

        val currentLangCode = availableLanguages[selectedLanguageIndex].first

        // Название курса на выбранном языке
        OutlinedTextField(
            value = course.title.content[currentLangCode] ?: "",
            onValueChange = { newTitle ->
                course = course.copy(
                    title = course.title.copy(
                        content = course.title.content.toMutableMap().apply {
                            this[currentLangCode] = newTitle
                        }
                    )
                )
            },
            label = { Text("Название курса (${availableLanguages[selectedLanguageIndex].second})") },
            modifier = Modifier.fillMaxWidth()
        )

        // Описание курса на выбранном языке
        OutlinedTextField(
            value = course.description.content[currentLangCode] ?: "",
            onValueChange = { newDescription ->
                course = course.copy(
                    description = course.description.copy(
                        content = course.description.content.toMutableMap().apply {
                            this[currentLangCode] = newDescription
                        }
                    )
                )
            },
            label = { Text("Описание курса (${availableLanguages[selectedLanguageIndex].second})") },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            maxLines = 10
        )

        // Информация о локализации
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Локализация контента",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Заполните контент на всех необходимых языках, переключаясь между вкладками.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Список языков с отметкой о заполненности
                availableLanguages.forEach { (code, name) ->
                    val hasTitleContent = !course.title.content[code].isNullOrBlank()
                    val hasDescriptionContent = !course.description.content[code].isNullOrBlank()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, modifier = Modifier.weight(1f))
                        Text(
                            if (hasTitleContent && hasDescriptionContent) "✓" else "✗",
                            color = if (hasTitleContent && hasDescriptionContent)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Кнопки действий
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    if (initialCourse == null) {
                        component.createCourse(course)
                    } else {
                        component.updateCourse(initialCourse.id, course)
                    }
                }
            ) {
                Text("Сохранить")
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = { component.navigateBack() }
            ) {
                Text("Отмена")
            }
        }
    }
}

/**
 * Создание пустого курса
 */
private fun createEmptyCourse() = CourseDomain(
    id = "",
    title = LocalizedContent(mapOf("ru" to "", "en" to "")),
    description = LocalizedContent(mapOf("ru" to "", "en" to "")),
    imageUrl = null,
    tags = emptyList(),
    visibility = CourseVisibilityDomain.PUBLIC,
    status = CourseStatusDomain.DRAFT,
    authorId = ""
)
