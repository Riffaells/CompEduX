package ui.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.course.CourseModuleDomain


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleItem(module: CourseModuleDomain, selectedLanguage: String) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Получаем контент для выбранного языка с запасными вариантами
            val title = module.title.content[selectedLanguage]
                ?: module.title.content["ru"]
                ?: module.title.content["en"]
                ?: module.title.content.values.firstOrNull()
                ?: "Модуль ${module.order}"

            // Заголовок модуля
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (module.description != null) {
                Spacer(modifier = Modifier.height(4.dp))

                // Безопасно получаем содержимое описания для выбранного языка
                val descriptionText = module.description?.content?.get(selectedLanguage)
                    ?: module.description?.content?.get("ru")
                    ?: module.description?.content?.get("en")
                    ?: module.description?.content?.values?.firstOrNull()
                    ?: ""

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Количество уроков с чипом
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                SuggestionChip(
                    onClick = { /* Переход к урокам */ },
                    label = {
                        Text(
                            text = "Уроков: ${module.lessons.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
            }
        }
    }
}