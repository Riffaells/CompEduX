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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
    initialCourse: CourseDomain?
) {
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

    // Используем MutableStateMap для хранения локализованного контента
    val titleContent = remember {
        mutableStateMapOf<String, String>().apply {
            initialCourse?.title?.content?.forEach { (lang, text) ->
                this[lang] = text
            }
            // Добавляем пустые значения для основных языков, если их нет
            if (!containsKey("ru")) this["ru"] = ""
            if (!containsKey("en")) this["en"] = ""
        }
    }

    val descriptionContent = remember {
        mutableStateMapOf<String, String>().apply {
            initialCourse?.description?.content?.forEach { (lang, text) ->
                this[lang] = text
            }
            // Добавляем пустые значения для основных языков, если их нет
            if (!containsKey("ru")) this["ru"] = ""
            if (!containsKey("en")) this["en"] = ""
        }
    }

    // Активные языки (те, для которых есть поля ввода)
    val activeLanguages = remember {
        mutableStateListOf<String>().apply {
            addAll(titleContent.keys)
        }
    }

    // Текущий выбранный язык для редактирования
    var selectedLanguage by remember { mutableStateOf("ru") }

    // Диалог добавления нового языка
    var isAddLanguageDialogVisible by remember { mutableStateOf(false) }

    var tags by remember {
        mutableStateOf(initialCourse?.tags?.joinToString(", ") ?: "")
    }
    var visibility by remember {
        mutableStateOf(initialCourse?.visibility ?: CourseVisibilityDomain.PRIVATE)
    }

    // Проверка валидности формы - русский заголовок и описание обязательны
    val isFormValid = titleContent["ru"]?.isNotBlank() == true && descriptionContent["ru"]?.isNotBlank() == true
    val hasRuTitleError = titleContent["ru"]?.isBlank() == true
    val hasRuDescriptionError = descriptionContent["ru"]?.isBlank() == true

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Языковая панель
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Чипы для выбора языка
                activeLanguages.forEach { langCode ->
                    val langName = availableLanguages.find { it.first == langCode }?.second ?: langCode
                    val isSelected = selectedLanguage == langCode
                    val isRequired = langCode == "ru"

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedLanguage = langCode },
                        label = {
                            Text(
                                text = langName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = if (isRequired) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected)
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        } else null
                    )
                }

                // Кнопка добавления языка
                IconButton(
                    onClick = { isAddLanguageDialogVisible = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить язык",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Секция заголовка для текущего языка
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val langName = availableLanguages.find { it.first == selectedLanguage }?.second ?: selectedLanguage
                val isRequired = selectedLanguage == "ru"
                val hasError = isRequired && hasRuTitleError

                Text(
                    text = "Название курса",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = titleContent[selectedLanguage] ?: "",
                    onValueChange = { titleContent[selectedLanguage] = it },
                    modifier = Modifier.fillMaxWidth(),
                    isError = hasError,
                    label = { Text(langName) },
                    supportingText = {
                        if (hasError) {
                            Text(
                                text = "Обязательное поле",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    placeholder = { Text("Введите название курса") },
                    trailingIcon = if (isRequired) {
                        {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else null
                )
            }
        }

        // Секция описания для текущего языка
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val langName = availableLanguages.find { it.first == selectedLanguage }?.second ?: selectedLanguage
                val isRequired = selectedLanguage == "ru"
                val hasError = isRequired && hasRuDescriptionError

                Text(
                    text = "Описание курса",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = descriptionContent[selectedLanguage] ?: "",
                    onValueChange = { descriptionContent[selectedLanguage] = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 7,
                    isError = hasError,
                    label = { Text(langName) },
                    supportingText = {
                        if (hasError) {
                            Text(
                                text = "Обязательное поле",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    placeholder = { Text("Введите описание курса") },
                    trailingIcon = if (isRequired) {
                        {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else null
                )
            }
        }

        // Секция дополнительных настроек
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
                    text = "Дополнительные настройки",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Теги
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Теги") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("python, программирование, начинающий") }
                )

                // Видимость курса
                Text(
                    text = "Видимость курса",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Карточки для выбора видимости
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CourseVisibilityDomain.values().forEach { visibilityOption ->
                        val isSelected = visibility == visibilityOption

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable { visibility = visibilityOption },
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when(visibilityOption) {
                                        CourseVisibilityDomain.PRIVATE -> "Приватный"
                                        CourseVisibilityDomain.UNLISTED -> "По ссылке"
                                        CourseVisibilityDomain.PUBLIC -> "Публичный"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = when(visibilityOption) {
                                        CourseVisibilityDomain.PRIVATE -> "Только вам"
                                        CourseVisibilityDomain.UNLISTED -> "По ссылке"
                                        CourseVisibilityDomain.PUBLIC -> "Всем"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки управления
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Кнопка отмены (только в режиме редактирования)
            if (initialCourse != null) {
                OutlinedButton(
                    onClick = { component.switchToViewMode() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }
            }

            // Кнопка сохранения
            Button(
                onClick = {
                    // Создаем локализованный контент из собранных данных
                    val title = LocalizedContent(titleContent.toMap())
                    val description = LocalizedContent(descriptionContent.toMap())

                    // Разбираем теги из строки
                    val tagList = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }

                    if (initialCourse != null) {
                        // Режим редактирования - обновляем существующий курс
                        val updatedCourse = initialCourse.copy(
                            title = title,
                            description = description,
                            tags = tagList,
                            visibility = visibility
                        )
                        component.updateCourse(updatedCourse)
                    } else {
                        // Режим создания - создаем новый курс
                        // Для создания используем только title и description, остальные поля заполнит UseCase
                        val dummyCourse = CourseDomain(
                            id = "",
                            title = title,
                            description = description,
                            authorId = "",
                            tags = tagList,
                            visibility = visibility,
                            status = CourseStatusDomain.DRAFT
                        )
                        component.createCourse(dummyCourse)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = isFormValid
            ) {
                Text(if (initialCourse != null) "Сохранить" else "Создать")
            }
        }
    }

    if (isAddLanguageDialogVisible) {
        AlertDialog(
            onDismissRequest = { isAddLanguageDialogVisible = false },
            title = { Text("Добавить язык") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(availableLanguages.filter { !activeLanguages.contains(it.first) }) { language ->
                        ListItem(
                            headlineContent = { Text("${language.second}") },
                            supportingContent = { Text("${language.first}") },
                            modifier = Modifier.clickable {
                                activeLanguages.add(language.first)
                                titleContent[language.first] = ""
                                descriptionContent[language.first] = ""
                                selectedLanguage = language.first
                                isAddLanguageDialogVisible = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isAddLanguageDialogVisible = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}