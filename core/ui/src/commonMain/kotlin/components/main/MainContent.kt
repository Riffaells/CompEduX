package components.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import component.app.main.HomeComponent
import component.app.main.MainComponent
import component.app.main.store.MainStore
import ui.CourseContent

/**
 * Главный контент приложения
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    component: MainComponent,
) {
    val state by component.state.collectAsState()

    Children(
        stack = component.childStack,
        animation = stackAnimation(
            fade() + scale()
        ),
        modifier = modifier
    ) { child ->
        when (val instance = child.instance) {
            is MainComponent.Child.HomeChild -> {
//                HomeScreen(
//                    component = instance.component,
//                    state = state,
//                    onRefresh = { component.onAction(MainStore.Intent.RefreshCourses) },
//                    onCourseClicked = { courseId -> component.navigateToCourse(courseId) },
//                    onCreateCourseClicked = { component.navigateToCourse(null) }
//                )
            }
            is MainComponent.Child.CourseChild -> {

                CourseContent(
                    component = instance.component,
                    modifier = Modifier
                )
            }
        }
    }
}
