import androidx.compose.ui.window.ComposeUIViewController
import com.riffaells.compedux.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
