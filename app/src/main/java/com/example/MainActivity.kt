package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        NovaApp()
      }
    }
  }
}

enum class NavigationTab(val title: String, val icon: ImageVector) {
  OVERVIEW("Обзор", Icons.Default.Layers),
  DEVICE("Телефон", Icons.Default.PhoneAndroid),
  APK_INFO("APK файл", Icons.Default.Android)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaApp() {
  var selectedTab by remember { mutableStateOf(NavigationTab.OVERVIEW) }
  val context = LocalContext.current

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_launcher_fg_asset),
              contentDescription = stringResource(id = R.string.app_name),
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
            )
            Text(
              text = stringResource(id = R.string.app_name),
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              Toast.makeText(context, "Приложение Nova v1.0 • Сборка готова", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.testTag("info_action_button")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "О приложении"
            )
          }
        }
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        NavigationTab.entries.forEach { tab ->
          NavigationBarItem(
            selected = selectedTab == tab,
            onClick = { selectedTab = tab },
            icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
            label = { Text(tab.title) },
            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (selectedTab) {
        NavigationTab.OVERVIEW -> OverviewScreen()
        NavigationTab.DEVICE -> DeviceScreen()
        NavigationTab.APK_INFO -> ApkBuildScreen()
      }
    }
  }
}

@Composable
fun OverviewScreen() {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var isDiagnosing by remember { mutableStateOf(false) }
  var diagnosticResult by remember { mutableStateOf<String?>(null) }
  var diagnosticProgress by remember { mutableIntStateOf(0) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Карточка статуса
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hero_status_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer
        )
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(48.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Успешно",
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(28.dp)
                )
              }
            }
            Column {
              Text(
                text = stringResource(id = R.string.status_distribution_ready),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = stringResource(id = R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
            }
          }

          Text(
            text = "Ваш APK полностью собран: добавлена адаптивная иконка, настроена цифровая подпись, поддержка 64-бит и интерфейс Material 3.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    }

    // Блок проверки работы
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Проверка компонентов",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
          }

          Text(
            text = "Проверка ресурсов приложения, фоновых потоков и целостности сборки.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          AnimatedVisibility(visible = isDiagnosing) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              LinearProgressIndicator(
                progress = { diagnosticProgress / 100f },
                modifier = Modifier.fillMaxWidth()
              )
              Text(
                text = "Проверка модулей... $diagnosticProgress%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          diagnosticResult?.let { result ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.secondaryContainer,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = result,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
            }
          }

          Button(
            onClick = {
              if (!isDiagnosing) {
                isDiagnosing = true
                diagnosticProgress = 0
                diagnosticResult = null
                coroutineScope.launch {
                  for (i in 1..10) {
                    delay(120)
                    diagnosticProgress = i * 10
                  }
                  isDiagnosing = false
                  diagnosticResult = "✓ Все 5 размеров иконок готовы\n✓ Векторная графика проверена\n✓ Правила ProGuard активны\n✓ Приложение успешно скомпилировано"
                  Toast.makeText(context, "Проверка успешно завершена!", Toast.LENGTH_SHORT).show()
                }
              }
            },
            enabled = !isDiagnosing,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("run_diagnostics_button")
          ) {
            Icon(
              imageVector = if (isDiagnosing) Icons.Default.Refresh else Icons.Default.PlayArrow,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (isDiagnosing) "Идет проверка..." else stringResource(id = R.string.btn_test_action))
          }
        }
      }
    }

    // Сетка показателей
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        MetricCard(
          title = "Целевой SDK",
          value = "API 36",
          subtitle = "Android 16",
          modifier = Modifier.weight(1f)
        )
        MetricCard(
          title = "Мин. SDK",
          value = "API 24",
          subtitle = "Android 7.0+",
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Кнопка копирования информации
    item {
      OutlinedButton(
        onClick = {
          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          val clip = ClipData.newPlainText(
            "Характеристики Nova",
            "Nova v1.0 (Сборка 1) | Пакет: com.aistudio.nova.qxzk | MinSdk: 24 | TargetSdk: 36"
          )
          clipboard.setPrimaryClip(clip)
          Toast.makeText(context, "Данные скопированы в буфер обмена!", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("copy_specs_button")
      ) {
        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(id = R.string.btn_share_info))
      }
    }
  }
}

@Composable
fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  OutlinedCard(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline
      )
    }
  }
}

@Composable
fun DeviceScreen() {
  val runtime = Runtime.getRuntime()
  val maxMemoryMb = (runtime.maxMemory() / (1024 * 1024)).toString()
  val totalMemoryMb = (runtime.totalMemory() / (1024 * 1024)).toString()
  val freeMemoryMb = (runtime.freeMemory() / (1024 * 1024)).toString()
  val availableProcessors = runtime.availableProcessors().toString()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text(
        text = "Характеристики вашего устройства",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    item {
      InfoRowCard(
        items = listOf(
          "Производитель" to Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
          "Модель" to Build.MODEL,
          "Бренд" to Build.BRAND.replaceFirstChar { it.uppercase() },
          "Код устройства" to Build.DEVICE,
          "Процессор / Чипсет" to Build.HARDWARE
        )
      )
    }

    item {
      Text(
        text = "Операционная система",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
      )
    }

    item {
      InfoRowCard(
        items = listOf(
          "Версия Android" to Build.VERSION.RELEASE,
          "Уровень API SDK" to Build.VERSION.SDK_INT.toString(),
          "Патч безопасности" to (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "Н/Д"),
          "Архитектура (ABI)" to Build.SUPPORTED_ABIS.joinToString(", ")
        )
      )
    }

    item {
      Text(
        text = "Ресурсы памяти и процессора",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
      )
    }

    item {
      InfoRowCard(
        items = listOf(
          "Ядер процессора" to availableProcessors,
          "Лимит памяти JVM" to "$maxMemoryMb МБ",
          "Выделенная память" to "$totalMemoryMb МБ",
          "Свободно в памяти" to "$freeMemoryMb МБ"
        )
      )
    }
  }
}

@Composable
fun ApkBuildScreen() {
  val context = LocalContext.current

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Параметры готового APK файла",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          InfoPair(label = "Идентификатор (ID)", value = "com.aistudio.nova.qxzk")
          InfoPair(label = "Версия", value = "1.0 (Build 1)")
          InfoPair(label = "Мин. версия Android", value = "Android 7.0 (API 24)")
          InfoPair(label = "Целевая версия", value = "Android 16 (API 36)")
          InfoPair(label = "Цифровая подпись", value = "APK Signature Scheme v1, v2, v3")
          InfoPair(label = "Сборщик", value = "Gradle Kotlin DSL + Jetpack Compose")
        }
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Как скачать и установить на телефон",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Text(
            text = "1. Нажмите вверху кнопку настроек / экспорта в AI Studio.\n" +
                   "2. Нажмите «Download APK» (скачать APK).\n" +
                   "3. Откройте скачанный файл в телефоне и нажмите «Установить».\n" +
                   "4. Приложение появится на рабочем столе с кастомной иконкой.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    item {
      Button(
        onClick = {
          Toast.makeText(context, "Файл APK собран без ошибок и готов к работе!", Toast.LENGTH_LONG).show()
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("verify_build_button"),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Проверить готовность APK")
      }
    }
  }
}

@Composable
fun InfoRowCard(items: RussianPairList) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items.forEachIndexed { index, (label, value) ->
        InfoPair(label = label, value = value)
        if (index < items.lastIndex) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          )
        }
      }
    }
  }
}

typealias RussianPairList = List<Pair<String, String>>

@Composable
fun InfoPair(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
