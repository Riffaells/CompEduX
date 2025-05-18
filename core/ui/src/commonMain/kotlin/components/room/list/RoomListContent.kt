package components.room.list

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import component.app.room.list.RoomListComponent
import model.room.RoomDomain
import ui.icon.RIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListContent(
    modifier: Modifier = Modifier,
    component: RoomListComponent
) {
    val state by component.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Заголовок и поиск
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Список комнат",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        component.filterRooms(it)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Поиск комнат") },
                    leadingIcon = { Icon(RIcons.Search, contentDescription = "Поиск") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { component.filterRooms(searchQuery) }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { showJoinDialog = true },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(RIcons.Add, contentDescription = "Присоединиться к комнате")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Присоединиться")
                }
            }
        }

        // Tabs для переключения между всеми комнатами и моими комнатами
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Все комнаты") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Мои комнаты") }
            )
        }

        // Список комнат
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val rooms = if (selectedTab == 0) state.rooms else state.myRooms
                
                if (rooms.isEmpty()) {
                    Text(
                        text = if (selectedTab == 0) "Нет доступных комнат" else "У вас нет комнат",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(rooms) { room ->
                            RoomItem(
                                room = room,
                                onRoomSelected = { component.selectRoom(room.id) }
                            )
                        }
                    }
                }
            }

            // Error message
            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }

            // FAB для создания новой комнаты
            FloatingActionButton(
                onClick = { component.createRoom() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(RIcons.Add, contentDescription = "Создать комнату")
            }
        }
    }

    // Диалог для присоединения к комнате по коду
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Присоединиться к комнате") },
            text = {
                Column {
                    Text("Введите код комнаты для присоединения:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = joinCode,
                        onValueChange = { joinCode = it },
                        placeholder = { Text("Код комнаты") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        component.joinRoom(joinCode)
                        showJoinDialog = false
                        joinCode = ""
                    },
                    enabled = joinCode.isNotBlank()
                ) {
                    Text("Присоединиться")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun RoomItem(
    room: RoomDomain,
    onRoomSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRoomSelected() }
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.name.getPreferredString(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                room.code?.let { code ->
                    OutlinedButton(
                        onClick = { /* Copy to clipboard */ },
                        modifier = Modifier.padding(start = 8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Код: $code", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            room.description?.let { description ->
                Text(
                    text = description.getPreferredString(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Participants count (simplified since we don't have participants in RoomDomain)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = RIcons.Person,
                        contentDescription = "Участники",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "0", // Simplified since we don't have participants count
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Owner indicator (using ownerId to determine ownership)
                if (room.ownerId.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Владелец",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
} 