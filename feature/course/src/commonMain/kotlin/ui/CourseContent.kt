package ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import components.CourseComponent
import ui.list.CourseListContent
import ui.view.CourseViewContent

/**
 * Основной контент для функционала курсов
 * Отображает текущий дочерний компонент из стека навигации
 */
@Composable
fun CourseContent(
    component: CourseComponent,
    modifier: Modifier = Modifier
) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(fade() + scale()),
    ) { child ->
        when (val instance = child.instance) {
            is CourseComponent.Child.CourseListChild -> CourseListContent(
                component = instance.component,
                parentComponent = component
            )

            is CourseComponent.Child.CourseViewChild -> CourseViewContent(
                component = instance.component,
                parentComponent = component
            )
            
            is CourseComponent.Child.CourseCreateChild -> CourseViewContent(
                component = instance.component,
                parentComponent = component
            )
        }
    }
} 