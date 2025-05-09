package di

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.singleton
import usecase.auth.*
import usecase.course.*
import usecase.rooms.*

/**
 * DI-модуль для домена
 * Содержит все Use Cases и другие компоненты доменного слоя
 */
val domainModule = DI.Module("domainModule") {
    // Auth Use Cases
    bind<LoginUseCase>() with singleton { LoginUseCase(instance()) }
    bind<RegisterUseCase>() with singleton { RegisterUseCase(instance()) }
    bind<GetCurrentUserUseCase>() with singleton { GetCurrentUserUseCase(instance()) }
    bind<LogoutUseCase>() with singleton { LogoutUseCase(instance()) }
    bind<CheckServerStatusUseCase>() with singleton { CheckServerStatusUseCase(instance()) }
    bind<IsAuthenticatedUseCase>() with singleton { IsAuthenticatedUseCase(instance()) }

    // Контейнер для всех use cases аутентификации
    bind<AuthUseCases>() with singleton {
        AuthUseCases(
            login = instance(),
            register = instance(),
            logout = instance(),
            getCurrentUser = instance(),
            isAuthenticated = instance(),
            checkServerStatus = instance()
        )
    }

    // Course Use Cases
    bindSingleton<GetCourseUseCase> { GetCourseUseCase(instance()) }
    bindSingleton<GetCoursesUseCase> { GetCoursesUseCase(instance()) }
    bindSingleton<CreateCourseUseCase> { CreateCourseUseCase(instance()) }
    bindSingleton<UpdateCourseUseCase> { UpdateCourseUseCase(instance()) }
    bindSingleton<DeleteCourseUseCase> { DeleteCourseUseCase(instance()) }

    // Контейнер для всех use cases курсов
    bind<CourseUseCases>() with singleton {
        CourseUseCases(
            getCourse = instance(),
            getCourses = instance(),
            createCourse = instance(),
            updateCourse = instance(),
            deleteCourse = instance()
        )
    }



    // Room Use Cases
    bindSingleton<GetRoomUseCase> { GetRoomUseCase(instance()) }
    bindSingleton<GetRoomsUseCase>{ GetRoomsUseCase(instance()) }
    bindSingleton<GetMyRoomsUseCase>{ GetMyRoomsUseCase(instance()) }
    bindSingleton<CreateRoomUseCase>{ CreateRoomUseCase(instance()) }
    bindSingleton<UpdateRoomUseCase>{ UpdateRoomUseCase(instance()) }
    bindSingleton<DeleteRoomUseCase>{ DeleteRoomUseCase(instance()) }

    // Контейнер для всех use cases курсов
    bind<RoomsUseCases>() with singleton {
        RoomsUseCases(
            getRoom = instance(),
            getRooms = instance(),
            getMyRooms = instance(),
            createRoom = instance(),
            updateRoom = instance(),
            deleteRoom = instance()
        )
    }
}
