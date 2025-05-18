package components.room.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import component.app.room.detail.RoomDetailComponent
import ui.icon.RIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailContent(
    modifier: Modifier = Modifier,
    component: RoomDetailComponent
) {
    val state by component.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    
    var roomName by remember(state.roomName) { mutableStateOf(state.roomName) }
    var roomDescription by remember(state.roomDescription) { mutableStateOf(state.roomDescription) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Заголовок с кнопкой назад
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { component.onBackClicked() }) {
                Icon(RIcons.ArrowBack, contentDescription = "Назад")
            }
            
            Text(
                text = if (isEditing) "Редактирование комнаты" else "Детали комнаты",
                style = MaterialTheme.typography.headlineSmall
            )
            
            if (state.isOwner) {
                IconButton(onClick = { isEditing = !isEditing }) {
                    Icon(
                        if (isEditing) RIcons.Close else RIcons.Edit, 
                        contentDescription = if (isEditing) "Отменить редактирование" else "Редактировать"
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp)) // Placeholder for alignment
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Информация о комнате
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (isEditing) {
                    // Редактирование названия
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Название комнаты") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Редактирование описания
                    OutlinedTextField(
                        value = roomDescription,
                        onValueChange = { roomDescription = it },
                        label = { Text("Описание комнаты") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Кнопки сохранения и отмены
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { 
                            isEditing = false
                            roomName = state.roomName
                            roomDescription = state.roomDescription
                        }) {
                            Text("Отмена")
                        }
                        
                        Button(
                            onClick = { 
                                component.updateRoomName(roomName)
                                component.updateRoomDescription(roomDescription)
                                component.saveRoom()
                                isEditing = false
                            },
                            enabled = roomName.isNotBlank()
                        ) {
                            Text("Сохранить")
                        }
                    }
                } else {
                    // Отображение названия
                    Text(
                        text = state.roomName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Отображение описания
                    Text(
                        text = state.roomDescription.ifBlank { "Нет описания" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Код комнаты
        if (state.roomCode.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Код комнаты",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = state.roomCode,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        IconButton(onClick = { /* Copy to clipboard */ }) {
                            Icon(RIcons.ContentCopy, contentDescription = "Копировать код")
                        }
                    }
                    
                    Text(
                        text = "Поделитесь этим кодом с другими пользователями, чтобы они могли присоединиться к комнате",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Информация об участниках
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Участники",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Количество участников: ${state.participants}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Кнопки действий
        if (state.isOwner) {
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(RIcons.Delete, contentDescription = "Удалить комнату")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Удалить комнату")
            }
        }
    }
    
    // Индикатор загрузки
    if (state.isLoading || state.isSaving) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    
    // Сообщение об ошибке
    state.error?.let { error ->
        Snackbar(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(error)
        }
    }
    
    // Диалог подтверждения удаления
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление комнаты") },
            text = { Text("Вы уверены, что хотите удалить эту комнату? Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = {
                        component.deleteRoom()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
} 