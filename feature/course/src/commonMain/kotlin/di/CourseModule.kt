package di

import api.course.NetworkCourseApi
import api.room.NetworkRoomApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import components.CourseComponent
import components.CourseComponentParams
import components.CourseStore
import components.CourseStoreFactory
import components.DefaultCourseComponent
import components.list.CourseListComponent
import components.list.CourseListComponentParams
import components.list.DefaultCourseListComponent
import components.view.CourseViewComponent
import components.view.CourseViewComponentParams
import components.view.DefaultCourseViewComponent
import logging.Logger
import logging.LoggingProvider
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindFactory
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.singleton
import repository.auth.TokenRepository
import repository.course.CourseRepository
import repository.course.CourseRepositoryImpl
import repository.room.RoomRepository

/**
 * DI модуль для функционала курсов
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
            storeFactory = instance(),
            courseId = params.courseId,
            onBackClicked = params.onBack,
            isCreateMode = params.isCreateMode,
            onCourseCreated = params.onCourseCreated,
            onCourseUpdated = params.onCourseUpdated
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