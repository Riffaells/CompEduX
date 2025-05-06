package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

/**
 * Система тем и стилей для дерева технологий.
 * Поддерживает:
 * - Светлую и темную тему
 * - Различные стили узлов (обычный, изученный, недоступный, выбранный)
 * - Различные стили соединений
 */
@Stable
class TreeTheme(
    // Основные цвета
    val background: Color,
    val surface: Color,
    val gridColor: Color,
    val textColor: Color,
    val textSelectedColor: Color,

    // Стили узлов
    val nodeStyles: NodeStyles,

    // Стили соединений
    val connectionStyles: ConnectionStyles,

    // Эффекты
    val effectStyles: EffectStyles,

    // Поддержка isDark для программного переключения
    val isDark: Boolean
) {
    /**
     * Стили узлов разных типов
     */
    @Stable
    class NodeStyles(
        // Стандартный узел
        val defaultNode: NodeStyle,

        // Выбранный узел
        val selectedNode: NodeStyle,

        // Изученный узел
        val completedNode: NodeStyle,

        // Доступный для изучения узел
        val availableNode: NodeStyle,

        // Недоступный узел
        val lockedNode: NodeStyle,

        // Различные формы
        val circularNode: NodeStyle,
        val hexagonNode: NodeStyle,
        val squareNode: NodeStyle
    ) {
        // Функция для получения стиля по типу узла
        fun getStyleForNode(
            nodeStyle: String,
            isSelected: Boolean,
            isCompleted: Boolean = false,
            isAvailable: Boolean = true
        ): NodeStyle {
            // Приоритет: выделение > доступность > форма
            return when {
                isSelected -> selectedNode
                !isAvailable -> lockedNode
                isCompleted -> completedNode
                isAvailable && !isCompleted -> availableNode
                nodeStyle == "circular" -> circularNode
                nodeStyle == "hexagon" -> hexagonNode
                nodeStyle == "square" -> squareNode
                else -> defaultNode
            }
        }
    }

    /**
     * Стиль для конкретного типа узла
     */
    @Stable
    class NodeStyle(
        val fillGradient: Brush,
        val strokeColor: Color,
        val glowColor: Color,
        val highlightColor: Color,
        val scale: Float = 1.0f,
        val pulseEffect: Boolean = false
    )

    /**
     * Стили соединений
     */
    @Stable
    class ConnectionStyles(
        val defaultConnection: ConnectionStyle,
        val highlightedConnection: ConnectionStyle,
        val dashedConnection: ConnectionStyle,
        val solidArrowConnection: ConnectionStyle
    ) {
        // Функция для получения стиля по типу соединения
        fun getStyleForConnection(
            connectionStyle: String,
            isHighlighted: Boolean
        ): ConnectionStyle {
            return if (isHighlighted) {
                highlightedConnection
            } else {
                when (connectionStyle) {
                    "dashed_line" -> dashedConnection
                    "solid_arrow" -> solidArrowConnection
                    else -> defaultConnection
                }
            }
        }
    }

    /**
     * Стиль для конкретного типа соединения
     */
    @Stable
    class ConnectionStyle(
        val strokeGradient: Brush,
        val glowColor: Color,
        val strokeWidth: Float,
        val dashPattern: FloatArray? = null
    )

    /**
     * Стили для различных эффектов
     */
    @Stable
    class EffectStyles(
        val nodeGlowAlpha: Float = 0.4f,
        val nodeHighlightAlpha: Float = 0.3f,
        val nodeShadowAlpha: Float = 0.5f,
        val connectionGlowAlpha: Float = 0.3f,
        val gridAlpha: Float = 0.15f,
        val backgroundGradientAlpha: Float = 0.1f
    )

    /**
     * Функция для копирования темы с измененными параметрами
     */
    fun copy(
        background: Color = this.background,
        surface: Color = this.surface,
        gridColor: Color = this.gridColor,
        textColor: Color = this.textColor,
        textSelectedColor: Color = this.textSelectedColor,
        nodeStyles: NodeStyles = this.nodeStyles,
        connectionStyles: ConnectionStyles = this.connectionStyles,
        effectStyles: EffectStyles = this.effectStyles,
        isDark: Boolean = this.isDark
    ): TreeTheme = TreeTheme(
        background = background,
        surface = surface,
        gridColor = gridColor,
        textColor = textColor,
        textSelectedColor = textSelectedColor,
        nodeStyles = nodeStyles,
        connectionStyles = connectionStyles,
        effectStyles = effectStyles,
        isDark = isDark
    )

    companion object {
        /**
         * Светлая тема для дерева технологий
         */
        val LightTheme = TreeTheme(
            background = Color(0xFFF5F5F5),
            surface = Color(0xFFFFFFFF),
            gridColor = Color(0xFF2A4494),
            textColor = Color(0xFF333333),
            textSelectedColor = Color(0xFF000000),
            nodeStyles = NodeStyles(
                defaultNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2))
                    ),
                    strokeColor = Color(0xFF0D47A1),
                    glowColor = Color(0xFF64B5F6),
                    highlightColor = Color.White
                ),
                selectedNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD54F), Color(0xFFFFA000))
                    ),
                    strokeColor = Color.White,
                    glowColor = Color(0xFFFFE082),
                    highlightColor = Color.White,
                    scale = 1.05f,
                    pulseEffect = true
                ),
                completedNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF66BB6A), Color(0xFF388E3C))
                    ),
                    strokeColor = Color(0xFF2E7D32),
                    glowColor = Color(0xFF81C784),
                    highlightColor = Color.White
                ),
                availableNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2))
                    ),
                    strokeColor = Color(0xFF0D47A1),
                    glowColor = Color(0xFF64B5F6),
                    highlightColor = Color.White
                ),
                lockedNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF78909C), Color(0xFF455A64))
                    ),
                    strokeColor = Color(0xFF263238),
                    glowColor = Color(0xFF90A4AE),
                    highlightColor = Color.White
                ),
                circularNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2))
                    ),
                    strokeColor = Color(0xFF0D47A1),
                    glowColor = Color(0xFF64B5F6),
                    highlightColor = Color.White
                ),
                hexagonNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF26A69A), Color(0xFF00796B))
                    ),
                    strokeColor = Color(0xFF004D40),
                    glowColor = Color(0xFF4DB6AC),
                    highlightColor = Color.White
                ),
                squareNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFEF5350), Color(0xFFD32F2F))
                    ),
                    strokeColor = Color(0xFFB71C1C),
                    glowColor = Color(0xFFE57373),
                    highlightColor = Color.White
                )
            ),
            connectionStyles = ConnectionStyles(
                defaultConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFF78909C).copy(alpha = 0.7f)),
                    glowColor = Color(0xFF90A4AE),
                    strokeWidth = 2f
                ),
                highlightedConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFFFFD54F).copy(alpha = 0.8f)),
                    glowColor = Color(0xFFFFE082),
                    strokeWidth = 3f
                ),
                dashedConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFF78909C).copy(alpha = 0.7f)),
                    glowColor = Color(0xFF90A4AE),
                    strokeWidth = 2f,
                    dashPattern = floatArrayOf(10f, 5f)
                ),
                solidArrowConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFF42A5F5).copy(alpha = 0.8f)),
                    glowColor = Color(0xFF64B5F6),
                    strokeWidth = 2.5f
                )
            ),
            effectStyles = EffectStyles(
                nodeGlowAlpha = 0.4f,
                nodeHighlightAlpha = 0.3f,
                nodeShadowAlpha = 0.4f,
                connectionGlowAlpha = 0.2f,
                gridAlpha = 0.1f,
                backgroundGradientAlpha = 0.05f
            ),
            isDark = false
        )

        /**
         * Темная тема для дерева технологий
         */
        val DarkTheme = TreeTheme(
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            gridColor = Color(0xFF2A4494),
            textColor = Color(0xFFE0E0E0),
            textSelectedColor = Color(0xFFFFFFFF),
            nodeStyles = NodeStyles(
                defaultNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
                    ),
                    strokeColor = Color(0xFF64B5F6),
                    glowColor = Color(0xFF2196F3),
                    highlightColor = Color(0xFFE3F2FD)
                ),
                selectedNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFC107), Color(0xFFFFB300))
                    ),
                    strokeColor = Color.White,
                    glowColor = Color(0xFFFFD54F),
                    highlightColor = Color(0xFFFFF9C4),
                    scale = 1.05f,
                    pulseEffect = true
                ),
                completedNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF43A047), Color(0xFF2E7D32))
                    ),
                    strokeColor = Color(0xFF81C784),
                    glowColor = Color(0xFF4CAF50),
                    highlightColor = Color(0xFFE8F5E9)
                ),
                availableNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
                    ),
                    strokeColor = Color(0xFF64B5F6),
                    glowColor = Color(0xFF2196F3),
                    highlightColor = Color(0xFFE3F2FD)
                ),
                lockedNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF546E7A), Color(0xFF37474F))
                    ),
                    strokeColor = Color(0xFF90A4AE),
                    glowColor = Color(0xFF607D8B),
                    highlightColor = Color(0xFFECEFF1)
                ),
                circularNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
                    ),
                    strokeColor = Color(0xFF64B5F6),
                    glowColor = Color(0xFF2196F3),
                    highlightColor = Color(0xFFE3F2FD)
                ),
                hexagonNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF00897B), Color(0xFF00695C))
                    ),
                    strokeColor = Color(0xFF4DB6AC),
                    glowColor = Color(0xFF009688),
                    highlightColor = Color(0xFFE0F2F1)
                ),
                squareNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFE53935), Color(0xFFC62828))
                    ),
                    strokeColor = Color(0xFFE57373),
                    glowColor = Color(0xFFF44336),
                    highlightColor = Color(0xFFFFEBEE)
                )
            ),
            connectionStyles = ConnectionStyles(
                defaultConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFF78909C).copy(alpha = 0.7f)),
                    glowColor = Color(0xFF607D8B),
                    strokeWidth = 2f
                ),
                highlightedConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFFFFC107).copy(alpha = 0.8f)),
                    glowColor = Color(0xFFFFD54F),
                    strokeWidth = 3f
                ),
                dashedConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFF78909C).copy(alpha = 0.7f)),
                    glowColor = Color(0xFF607D8B),
                    strokeWidth = 2f,
                    dashPattern = floatArrayOf(10f, 5f)
                ),
                solidArrowConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFF1E88E5).copy(alpha = 0.8f)),
                    glowColor = Color(0xFF42A5F5),
                    strokeWidth = 2.5f
                )
            ),
            effectStyles = EffectStyles(
                nodeGlowAlpha = 0.5f,
                nodeHighlightAlpha = 0.4f,
                nodeShadowAlpha = 0.6f,
                connectionGlowAlpha = 0.3f,
                gridAlpha = 0.2f,
                backgroundGradientAlpha = 0.15f
            ),
            isDark = true
        )

        /**
         * Корпоративная тема для дерева технологий
         */
        val CorporateTheme = TreeTheme(
            background = Color(0xFF1A2233),
            surface = Color(0xFF243147),
            gridColor = Color(0xFF3A506B),
            textColor = Color(0xFFE0E0E0),
            textSelectedColor = Color(0xFFFFFFFF),
            nodeStyles = NodeStyles(
                defaultNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF0066CC), Color(0xFF004C99))
                    ),
                    strokeColor = Color(0xFF66B2FF),
                    glowColor = Color(0xFF0080FF),
                    highlightColor = Color(0xFFE6F0FF)
                ),
                selectedNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFCC00), Color(0xFFE6B800))
                    ),
                    strokeColor = Color.White,
                    glowColor = Color(0xFFFFD633),
                    highlightColor = Color(0xFFFFF9E6),
                    scale = 1.05f,
                    pulseEffect = true
                ),
                completedNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF4CAF50), Color(0xFF388E3C))
                    ),
                    strokeColor = Color(0xFF81C784),
                    glowColor = Color(0xFF66BB6A),
                    highlightColor = Color(0xFFE8F5E9)
                ),
                availableNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF0066CC), Color(0xFF004C99))
                    ),
                    strokeColor = Color(0xFF66B2FF),
                    glowColor = Color(0xFF0080FF),
                    highlightColor = Color(0xFFE6F0FF)
                ),
                lockedNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF607D8B), Color(0xFF455A64))
                    ),
                    strokeColor = Color(0xFF90A4AE),
                    glowColor = Color(0xFF78909C),
                    highlightColor = Color(0xFFECEFF1)
                ),
                circularNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF0066CC), Color(0xFF004C99))
                    ),
                    strokeColor = Color(0xFF66B2FF),
                    glowColor = Color(0xFF0080FF),
                    highlightColor = Color(0xFFE6F0FF)
                ),
                hexagonNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF009688), Color(0xFF00796B))
                    ),
                    strokeColor = Color(0xFF4DB6AC),
                    glowColor = Color(0xFF26A69A),
                    highlightColor = Color(0xFFE0F2F1)
                ),
                squareNode = NodeStyle(
                    fillGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFF44336), Color(0xFFD32F2F))
                    ),
                    strokeColor = Color(0xFFE57373),
                    glowColor = Color(0xFFEF5350),
                    highlightColor = Color(0xFFFFEBEE)
                )
            ),
            connectionStyles = ConnectionStyles(
                defaultConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFF90A4AE).copy(alpha = 0.7f)),
                    glowColor = Color(0xFF78909C),
                    strokeWidth = 2f
                ),
                highlightedConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFFFFCC00).copy(alpha = 0.8f)),
                    glowColor = Color(0xFFFFD633),
                    strokeWidth = 3f
                ),
                dashedConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFF90A4AE).copy(alpha = 0.7f)),
                    glowColor = Color(0xFF78909C),
                    strokeWidth = 2f,
                    dashPattern = floatArrayOf(10f, 5f)
                ),
                solidArrowConnection = ConnectionStyle(
                    strokeGradient = SolidColor(Color(0xFF0080FF).copy(alpha = 0.8f)),
                    glowColor = Color(0xFF0099FF),
                    strokeWidth = 2.5f
                )
            ),
            effectStyles = EffectStyles(
                nodeGlowAlpha = 0.5f,
                nodeHighlightAlpha = 0.4f,
                nodeShadowAlpha = 0.6f,
                connectionGlowAlpha = 0.3f,
                gridAlpha = 0.2f,
                backgroundGradientAlpha = 0.15f
            ),
            isDark = true
        )
    }
}

/**
 * Composable функция для получения текущей темы дерева технологий
 */
@Composable
fun rememberTreeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useCorporateTheme: Boolean = false
): TreeTheme {
    // Здесь можно разместить логику выбора темы в зависимости от настроек
    return remember(darkTheme, useCorporateTheme) {
        when {
            useCorporateTheme -> TreeTheme.CorporateTheme
            darkTheme -> TreeTheme.DarkTheme
            else -> TreeTheme.LightTheme
        }
    }
}

/**
 * Обертка для предоставления темы дерева технологий
 */
@Composable
fun TreeThemeProvider(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useCorporateTheme: Boolean = false,
    content: @Composable (TreeTheme) -> Unit
) {
    val treeTheme = rememberTreeTheme(darkTheme, useCorporateTheme)
    content(treeTheme)
}
