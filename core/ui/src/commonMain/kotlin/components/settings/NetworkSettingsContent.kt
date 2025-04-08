package components.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import compedux.core.ui.generated.resources.*
import component.settings.CategoryBlock
import component.settings.ExperimentalBadge
import component.settings.SettingTextField
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import settings.MultiplatformSettings
import ui.icon.RIcons
import components.settings.network.*
import components.settings.network.components.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.snapshotFlow
import domain.MultiplatformSettings
import components.settings.network.ProtocolsSection
import components.settings.network.SecuritySection
import component.settings.ExpandableButton
import component.settings.FuturisticFeatureCard
import component.settings.FuturisticFilterChip
import component.settings.FuturisticOptionChip
import component.settings.FuturisticProgressBar
import component.settings.NewFeatureBadge
import component.settings.SectionHeader

/**
 * Composable функция, отображающая настройки сети
 *
 * @param serverUrl Текущий URL сервера
 * @param onServerUrlChanged Callback при изменении URL сервера
 * @param isExperimentalApiEnabled Включено ли использование экспериментального API
 * @param onExperimentalApiEnabledChanged Callback при изменении опции экспериментального API
 * @param isBandwidthLimitEnabled Включено ли ограничение пропускной способности
 * @param onBandwidthLimitEnabledChanged Callback при изменении статуса ограничения пропускной способности
 * @param bandwidthLimitKbps Значение ограничения пропускной способности в Кбит/с
 * @param onBandwidthLimitChanged Callback при изменении значения ограничения пропускной способности
 * @param selectedProtocol Выбранный сетевой протокол
 * @param onProtocolSelected Callback при выборе протокола
 * @param isCertificateValidationEnabled Включена ли валидация сертификатов
 * @param onCertificateValidationChanged Callback при изменении статуса валидации сертификатов
 * @param authToken Токен авторизации
 * @param onAuthTokenChanged Callback при изменении токена авторизации
 * @param modifier Модификатор для настройки внешнего вида
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun NetworkSettingsContent(
    serverUrl: String,
    onServerUrlChanged: (String) -> Unit,
    isExperimentalApiEnabled: Boolean,
    onExperimentalApiEnabledChanged: (Boolean) -> Unit,
    isBandwidthLimitEnabled: Boolean,
    onBandwidthLimitEnabledChanged: (Boolean) -> Unit,
    bandwidthLimitKbps: Int,
    onBandwidthLimitChanged: (Int) -> Unit,
    selectedProtocol: NetworkProtocol,
    onProtocolSelected: (NetworkProtocol) -> Unit,
    isCertificateValidationEnabled: Boolean,
    onCertificateValidationChanged: (Boolean) -> Unit,
    authToken: String,
    onAuthTokenChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Заголовок секции
        CategoryBlock(
            title = stringResource(Res.string.network_settings_title),
            description = stringResource(Res.string.network_settings_description),
            icon = RIcons.Network,
            modifier = Modifier.fillMaxWidth()
        )

        // Серверные настройки (URL, API)
        ServerSettingsSection(
            serverUrl = serverUrl,
            onServerUrlChange = onServerUrlChanged,
            useExperimentalApi = isExperimentalApiEnabled,
            onExperimentalApiChange = onExperimentalApiEnabledChanged,
            enableBandwidthLimit = isBandwidthLimitEnabled,
            onEnableBandwidthLimitChange = onBandwidthLimitEnabledChanged,
            bandwidthLimitKbps = bandwidthLimitKbps.toFloat(),
            onBandwidthLimitChange = { onBandwidthLimitChanged(it.toInt()) },
            isExpandedBandwidth = false,
            onExpandedBandwidthChange = { /* do nothing */ },
            modifier = Modifier.fillMaxWidth()
        )

        // Секция протоколов
        ProtocolsSection(
            selectedProtocol = selectedProtocol,
            onProtocolSelected = onProtocolSelected,
            modifier = Modifier.fillMaxWidth()
        )

        // Секция безопасности
        SecuritySection(
            isCertificateValidationEnabled = isCertificateValidationEnabled,
            onCertificateValidationChanged = onCertificateValidationChanged,
            authToken = authToken,
            onAuthTokenChanged = onAuthTokenChanged,
            modifier = Modifier.fillMaxWidth()
        )

        // Настройки ограничения пропускной способности
        BandwidthSection(
            isBandwidthLimitEnabled = isBandwidthLimitEnabled,
            onBandwidthLimitEnabledChanged = onBandwidthLimitEnabledChanged,
            bandwidthLimitKbps = bandwidthLimitKbps,
            onBandwidthLimitChanged = onBandwidthLimitChanged,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Composable функция, отображающая настройки сети с управлением состоянием
 *
 * @param settings Экземпляр MultiplatformSettings для хранения настроек
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun NetworkSettingsContentWithState(
    settings: MultiplatformSettings,
    modifier: Modifier = Modifier
) {
    // Получаем настройки сети
    val networkSettings = settings.networkSettings

    // Состояние настроек
    var serverUrl by remember { mutableStateOf(networkSettings.serverUrl) }
    var useExperimentalApi by remember { mutableStateOf(networkSettings.useExperimentalApi) }
    var enableBandwidthLimit by remember { mutableStateOf(networkSettings.enableBandwidthLimit) }
    var bandwidthLimitKbps by remember { mutableStateOf(networkSettings.bandwidthLimitKbps) }
    var selectedProtocol by remember { mutableStateOf(NetworkProtocol.HTTPS) }
    var isCertificateValidationEnabled by remember { mutableStateOf(true) }
    var authToken by remember { mutableStateOf("") }
    var isExpandedBandwidth by remember { mutableStateOf(false) }

    // Сохраняем изменения в настройках
    LaunchedEffect(serverUrl) {
        networkSettings.serverUrl = serverUrl
    }

    LaunchedEffect(useExperimentalApi) {
        networkSettings.useExperimentalApi = useExperimentalApi
    }

    LaunchedEffect(enableBandwidthLimit) {
        networkSettings.enableBandwidthLimit = enableBandwidthLimit
    }

    LaunchedEffect(bandwidthLimitKbps) {
        networkSettings.bandwidthLimitKbps = bandwidthLimitKbps
    }

    // Наблюдаем за изменениями настроек из других источников
    LaunchedEffect(Unit) {
        snapshotFlow { networkSettings.serverUrl }.collectLatest {
            if (it != serverUrl) serverUrl = it
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { networkSettings.useExperimentalApi }.collectLatest {
            if (it != useExperimentalApi) useExperimentalApi = it
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { networkSettings.enableBandwidthLimit }.collectLatest {
            if (it != enableBandwidthLimit) enableBandwidthLimit = it
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { networkSettings.bandwidthLimitKbps }.collectLatest {
            if (it != bandwidthLimitKbps) bandwidthLimitKbps = it
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Заголовок секции
        CategoryBlock(
            title = stringResource(Res.string.network_settings_title),
            description = stringResource(Res.string.network_settings_description),
            icon = RIcons.Network,
            modifier = Modifier.fillMaxWidth()
        )

        // Серверные настройки (URL, API)
        ServerSettingsSection(
            serverUrl = serverUrl,
            onServerUrlChange = { serverUrl = it },
            useExperimentalApi = useExperimentalApi,
            onExperimentalApiChange = { useExperimentalApi = it },
            enableBandwidthLimit = enableBandwidthLimit,
            onEnableBandwidthLimitChange = { enableBandwidthLimit = it },
            bandwidthLimitKbps = bandwidthLimitKbps.toFloat(),
            onBandwidthLimitChange = { bandwidthLimitKbps = it.toInt() },
            isExpandedBandwidth = isExpandedBandwidth,
            onExpandedBandwidthChange = { isExpandedBandwidth = it },
            modifier = Modifier.fillMaxWidth()
        )

        // Секция протоколов
        ProtocolsSection(
            selectedProtocol = selectedProtocol,
            onProtocolSelected = { selectedProtocol = it },
            modifier = Modifier.fillMaxWidth()
        )

        // Секция безопасности
        SecuritySection(
            isCertificateValidationEnabled = isCertificateValidationEnabled,
            onCertificateValidationChanged = { isCertificateValidationEnabled = it },
            authToken = authToken,
            onAuthTokenChanged = { authToken = it },
            modifier = Modifier.fillMaxWidth()
        )

        // Настройки ограничения пропускной способности
        BandwidthSection(
            isBandwidthLimitEnabled = enableBandwidthLimit,
            onBandwidthLimitEnabledChanged = { enableBandwidthLimit = it },
            bandwidthLimitKbps = bandwidthLimitKbps,
            onBandwidthLimitChanged = { bandwidthLimitKbps = it },
            modifier = Modifier.fillMaxWidth()
        )

        // Дополнительная информация о разработке
        components.settings.network.components.DevelopmentStatus(
            progress = 0.35f,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )

        // Планируемые функции
        components.settings.network.components.PlannedFeatures(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        // Spacer для лучшего скроллинга
        Spacer(modifier = Modifier.height(32.dp))
    }
}
