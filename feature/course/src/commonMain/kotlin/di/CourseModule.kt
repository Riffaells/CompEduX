package di

import api.course.NetworkCourseApi
import components.*
import components.list.CourseListComponent
import components.list.CourseListComponentParams
import components.list.DefaultCourseListComponent
import components.view.CourseViewComponent
import components.view.CourseViewComponentParams
import components.view.DefaultCourseViewComponent
import logging.LoggingProvider
import org.kodein.di.*
import repository.auth.TokenRepository
import repository.course.CourseRepository
import repository.course.CourseRepositoryImpl

/**
 * Модуль для предоставления зависимостей, связанных с курсами
 */
val courseModule = DI.Module("courseModule") {
    // Компоненты
    bindFactory<CourseComponentParams, CourseComponent> { params ->
        DefaultCourseComponent(
            di = di,
            componentContext = params.componentContext,
            onBack = params.onBackClicked
        )
    }

    bindFactory<CourseListComponentParams, CourseListComponent> { params ->
        DefaultCourseListComponent(
            componentContext = params.componentContext,
            di = di,
            storeFactory = instance(),
            onBackClicked = params.onBack,
            onCourseSelected = params.onCourseSelected
        )
    }

    bindFactory<CourseViewComponentParams, CourseViewComponent> { params ->
        DefaultCourseViewComponent(
            componentContext = params.componentContext,
            di = di,
            onBack = params.onBack,
            isCreateMode = params.isCreateMode,
            initialCourseId = params.courseId
        )
    }

    // Stores
    bindProvider<CourseStore> {
        CourseStoreFactory(
            storeFactory = instance(),
            di = di
        ).create()
    }

    // Репозитории и адаптеры для курсов
    bindSingleton<CourseRepository> {
        val networkCourseApi = instance<NetworkCourseApi>()
        val tokenRepository = instance<TokenRepository>()
        val logger = instance<LoggingProvider>().getLogger("CourseRepository")
        CourseRepositoryImpl(networkCourseApi, tokenRepository, logger)
    }
}