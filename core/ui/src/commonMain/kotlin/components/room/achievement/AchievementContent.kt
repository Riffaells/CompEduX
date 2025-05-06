package components.room.achievement

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import component.app.room.achievement.AchievementComponent
import component.app.room.achievement.store.AchievementStore

@Composable
fun AchievementContent(component: AchievementComponent) {
    val state by component.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with refresh button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ваши достижения",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = { component.onAction(AchievementStore.Intent.RefreshAchievements) }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Обновить достижения"
                )
            }
        }

        // Achievement list
        if (state.achievements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет доступных достижений",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.achievements) { achievement ->
                    AchievementItem(
                        achievement = achievement,
                        onUnlock = { component.onAction(AchievementStore.Intent.UnlockAchievement(achievement.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementItem(
    achievement: AchievementStore.Achievement,
    onUnlock: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    // Animation for progress
    val progressAnimation = remember(achievement.id, achievement.progress) {
        Animatable(initialValue = 0f)
    }

    LaunchedEffect(achievement.id, achievement.progress) {
        progressAnimation.animateTo(
            targetValue = achievement.progress / achievement.maxProgress,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        onClick = { showDetails = !showDetails }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Achievement header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Achievement icon/status
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (achievement.isUnlocked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 2.dp,
                            color = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (achievement.isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                        contentDescription = if (achievement.isUnlocked) "Разблокировано" else "Заблокировано",
                        tint = if (achievement.isUnlocked) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Achievement title and status
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (achievement.isUnlocked) "Разблокировано" else "Прогресс: ${achievement.progress.toInt()}/${achievement.maxProgress.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Unlock button (only for achievements in progress)
                if (!achievement.isUnlocked && achievement.progress > 0) {
                    Button(
                        onClick = onUnlock,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Разблокировать")
                    }
                }
            }

            // Progress bar
            if (!achievement.isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressAnimation.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            // Expanded details
            AnimatedVisibility(
                visible = showDetails,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "Описание:",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
