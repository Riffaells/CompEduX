package components.room

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import component.app.room.RoomComponent
import component.app.room.store.RoomStore
import components.room.achievement.AchievementContent
import components.room.detail.RoomDetailContent
import components.room.diagram.DiagramContent
import components.room.list.RoomListContent
import ui.icon.RIcons

/**
 * Композабл для отображения комнаты с диаграммами и достижениями
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomContent(
    modifier: Modifier = Modifier,
    component: RoomComponent
) {
    // Получаем состояние из компонента
    val state by component.state.collectAsState()
    val diagramSlot by component.diagramSlot.subscribeAsState()
    val achievementSlot by component.achievementSlot.subscribeAsState()
    val roomStack by component.roomStack.subscribeAsState()

    // Состояние для анимации элементов
    var showContent by remember { mutableStateOf(false) }

    // Запускаем анимацию появления контента с небольшой задержкой
    LaunchedEffect(Unit) {
        showContent = true
    }

    // Анимация для контента
    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "ContentAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Комнаты") },
                navigationIcon = {
                    IconButton(onClick = { component.onBackClicked() }) {
                        Icon(RIcons.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { component.onEvent(RoomStore.Intent.ToggleAchievement) }) {
                        Icon(
                            imageVector = RIcons.Add,
                            contentDescription = if (state.showAchievement) "Скрыть достижения" else "Показать достижения"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Основной контент с комнатами, диаграммами и достижениями
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .alpha(contentAlpha),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Основная область для списка комнат или детального просмотра
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                    ) {
                        when (val child = roomStack.active.instance) {
                            is RoomComponent.RoomChild.List -> {
                                RoomListContent(component = child.component)
                            }
                            is RoomComponent.RoomChild.Detail -> {
                                RoomDetailContent(component = child.component)
                            }
                        }
                    }
                }

                // Правая панель с диаграммой и достижениями
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Диаграмма - всегда отображается
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        diagramSlot.child?.instance?.let { diagramChild ->
                            when (diagramChild) {
                                is RoomComponent.DiagramChild.DiagramContent -> {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .shadow(4.dp, RoundedCornerShape(16.dp))
                                    ) {
                                        Column {
                                            Text(
                                                text = "Диаграмма",
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(8.dp),
                                                textAlign = TextAlign.Center
                                            )
                                            DiagramContent(diagramChild.component)
                                        }
                                    }
                                }
                            }
                        } ?: run {
                            // Placeholder when no diagram is shown
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Диаграмма загружается...")
                            }
                        }
                    }

                    // Достижения
                    AnimatedVisibility(
                        visible = state.showAchievement,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            achievementSlot.child?.instance?.let { achievementChild ->
                                when (achievementChild) {
                                    is RoomComponent.AchievementChild.AchievementContent -> {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Достижения",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                                        .padding(8.dp),
                                                    textAlign = TextAlign.Center
                                                )
                                                AchievementContent(achievementChild.component)
                                            }
                                        }
                                    }
                                }
                            } ?: run {
                                // Placeholder when no achievements are shown
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Достижения скрыты")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
