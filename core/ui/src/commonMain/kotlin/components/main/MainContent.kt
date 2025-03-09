package components.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.app.main.MainComponent
import component.app.main.store.MainStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.skia.Font
import org.jetbrains.skia.Typeface
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Композабл для отображения главного экрана
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(component: MainComponent) {
    // Получаем состояние из компонента
    val state by component.state.collectAsState()

    // Состояние для управления drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Состояние для анимации элементов
    var showContent by remember { mutableStateOf(false) }
    var showSkiaDemo by remember { mutableStateOf(false) }

    // Запускаем анимацию появления контента с небольшой задержкой
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    // Анимация масштаба для кнопок
    val buttonScale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.8f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "ButtonScale"
    )

    // Создаем постоянный drawer с дополнительными опциями
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Дополнительные опции",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                Divider()

                // Анимированные элементы drawer
                val drawerItems = listOf(
                    "Профиль" to { scope.launch { drawerState.close() } },
                    "Избранное" to { scope.launch { drawerState.close() } },
                    "История" to { scope.launch { drawerState.close() } }
                )

                drawerItems.forEachIndexed { index, (text, onClick) ->
                    var showItem by remember { mutableStateOf(false) }

                    // Запускаем анимацию с задержкой для каждого элемента
                    LaunchedEffect(drawerState.isOpen) {
                        if (drawerState.isOpen) {
                            delay(100L * index)
                            showItem = true
                        } else {
                            showItem = false
                        }
                    }

                    AnimatedVisibility(
                        visible = showItem,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        NavigationDrawerItem(
                            label = { Text(text) },
                            selected = false,
                            onClick = { onClick() }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(state.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Анимация появления текста
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(500)) +
                            expandVertically(animationSpec = tween(500)),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Главный экран",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Анимация появления кнопок
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(700)) +
                            slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(700)
                            ),
                    exit = fadeOut()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = { component.onAction(MainStore.Intent.OpenSettings) },
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .scale(buttonScale)
                        ) {
                            Text("Открыть настройки")
                        }

                        Button(
                            onClick = { component.onAction(MainStore.Intent.UpdateTitle("Обновленный заголовок")) },
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .scale(buttonScale)
                        ) {
                            Text("Обновить заголовок")
                        }

                        Button(
                            onClick = { showSkiaDemo = !showSkiaDemo },
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .scale(buttonScale)
                        ) {
                            Text(if (showSkiaDemo) "Скрыть Skia демо" else "Показать Skia демо")
                        }

                        // Добавляем кнопку для открытия карты развития
                        Button(
                            onClick = { /*component.onAction(MainStore.Intent.OpenDevelopmentMap)*/ },
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .scale(buttonScale)
                        ) {
                            Text("Открыть карту развития")
                        }
                    }
                }

                // Skia демонстрация
                AnimatedVisibility(
                    visible = showSkiaDemo,
                    enter = fadeIn(animationSpec = tween(500)),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    ) {
                        SkiaDemo()
                    }
                }
            }
        }
    }
}

/**
 * Демонстрация возможностей Skia
 */
@Composable
fun SkiaDemo() {
    // Создаем бесконечную анимацию
    val infiniteTransition = rememberInfiniteTransition(label = "SkiaAnimation")

    // Добавляем TextMeasurer для измерения и рисования текста
    val textMeasurer = rememberTextMeasurer()

    // Анимация вращения
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Анимация пульсации
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Анимация цвета
    val hue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hue"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 4 * scale

        // Рисуем фон с градиентом
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.hsl((hue * 360f) % 360f, 0.7f, 0.3f),
                    Color.hsl(((hue * 360f) + 180f) % 360f, 0.7f, 0.3f)
                ),
                center = center,
                radius = size.maxDimension
            ),
            size = size
        )

        // Рисуем вращающуюся звезду
        rotate(rotation, center) {
            val path = Path()
            val points = 12
            val innerRadius = radius * 0.4f

            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) radius else innerRadius
                val angle = (i * PI / points).toFloat()
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()

            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(
                    width = 8f,
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), (hue * 30f) % 30f)
                )
            )
        }

        // Рисуем орбиты с планетами
        for (i in 1..3) {
            val orbitRadius = radius * (0.6f + i * 0.3f)

            // Орбита
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = orbitRadius,
                center = center,
                style = Stroke(width = 2f)
            )

            // Планета
            val planetAngle = (rotation + i * 120) % 360 * (PI / 180f).toFloat()
            val planetX = center.x + orbitRadius * cos(planetAngle)
            val planetY = center.y + orbitRadius * sin(planetAngle)

            drawCircle(
                color = Color.hsl(((hue * 360f) + i * 120f) % 360f, 0.8f, 0.7f),
                radius = 20f * scale,
                center = Offset(planetX, planetY)
            )
        }

        // Рисуем пульсирующие круги в центре
        for (i in 1..5) {
            val pulseScale = (scale + i * 0.1f) % 1.5f
            drawCircle(
                color = Color.White.copy(alpha = (1f - pulseScale) * 0.5f),
                radius = radius * pulseScale * 0.5f,
                center = center
            )
        }

        // Рисуем текст
        val textSize = 24.dp.toPx()
        translate(center.x - textSize * 2, center.y) {
            scale(scale) {
                // Используем TextMeasurer для рисования текста
                drawText(
                    textMeasurer = textMeasurer,
                    text = "Skia Demo",
                    topLeft = Offset(0f, 0f),
                    style = TextStyle(
                        fontSize = textSize.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
