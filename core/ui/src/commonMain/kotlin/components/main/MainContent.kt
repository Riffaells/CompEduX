package components.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import component.app.main.HomeComponent
import component.app.main.MainComponent
import components.course.ModernUnifiedCourseContent

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
                HomeScreen(
                    component = instance.component,
                    onCourseClicked = { courseId -> /*component.onCourseSelected(courseId)*/ },
                    onCreateCourseClicked = { /*component.onCreateCourseClicked()*/ }
                )
            }
            is MainComponent.Child.CourseChild -> {
                val courseComponent = instance.component
                val courseState by courseComponent.state.collectAsState()



                ModernUnifiedCourseContent(
                    courseId = courseState.courseId,
                    isLoading = courseState.isLoading,
                    error = courseState.error,
                    course = courseState.course,
                    modules = courseState.modules,
                    isEditMode = false, // courseState.isEditMode,
                    isModified = false, //courseState.isModified,
                    onCreateCourse = { /*courseComponent::createCourse*/ },
                    onSaveCourse = { /*courseComponent::saveCourse,*/ },
                    onLoadCourse = { /*courseComponent::loadCourse,*/ },
                    onToggleEditMode = { /*courseComponent::toggleEditMode,*/ },
                    onDiscardChanges = { /*courseComponent::discardChanges,*/ },
                    onAddModule = { /*courseComponent::addModule,*/ },
                    onUpdateModule = { /*courseComponent::updateModule,*/ },
                    onDeleteModule = { /*courseComponent::deleteModule,*/ },
                    onMoveModuleUp = { /*courseComponent::moveModuleUp,*/ },
                    onMoveModuleDown = { /*courseComponent::moveModuleDown,*/ },
                    onAddLesson = { /*courseComponent::addLesson,*/ },
                    onEditLesson = {_,_ -> /*courseComponent::editLesson,*/ },
                    onDeleteLesson = {_,_ ->  /*courseComponent::deleteLesson,*/ },
                    onModuleClick = courseComponent::navigateToModule,
                    onNavigateBack = { /*component::onBackClicked*/ }
                )
            }
        }
    }
}

/**
 * Заглушка домашнего экрана
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    component: HomeComponent,
    onCourseClicked: (String) -> Unit,
    onCreateCourseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text("Домашний экран будет реализован позже")
        }
    }
}