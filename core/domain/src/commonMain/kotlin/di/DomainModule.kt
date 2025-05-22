package di

import org.kodein.di.*
import usecase.auth.*
import usecase.course.*
import usecase.room.*
import usecase.tree.*

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


    // Technology Tree Use Cases
    bindSingleton<GetTreeForCourseUseCase> { GetTreeForCourseUseCase(instance()) }
    bindSingleton<CreateTreeUseCase> { CreateTreeUseCase(instance()) }
    bindSingleton<UpdateTreeUseCase> { UpdateTreeUseCase(instance()) }
    bindSingleton<DeleteTreeUseCase> { DeleteTreeUseCase(instance()) }
    bindSingleton<AddNodeUseCase> { AddNodeUseCase(instance()) }
    bindSingleton<UpdateNodeUseCase> { UpdateNodeUseCase(instance()) }
    bindSingleton<RemoveNodeUseCase> { RemoveNodeUseCase(instance()) }
    bindSingleton<AddConnectionUseCase> { AddConnectionUseCase(instance()) }
    bindSingleton<UpdateConnectionUseCase> { UpdateConnectionUseCase(instance()) }
    bindSingleton<RemoveConnectionUseCase> { RemoveConnectionUseCase(instance()) }

    // Контейнер для всех use cases технологического дерева
    bindSingleton<TreeUseCases> {
        TreeUseCases(
            getTreeForCourse = instance(),
            createTree = instance(),
            updateTree = instance(),
            deleteTree = instance(),
            addNode = instance(),
            updateNode = instance(),
            removeNode = instance(),
            addConnection = instance(),
            updateConnection = instance(),
            removeConnection = instance()
        )
    }

    // Room Use Cases
    bindSingleton<GetRoomUseCase> { GetRoomUseCase(instance()) }
    bindSingleton<GetRoomsUseCase> { GetRoomsUseCase(instance()) }
    bindSingleton<GetMyRoomsUseCase> { GetMyRoomsUseCase(instance()) }
    bindSingleton<CreateRoomUseCase> { CreateRoomUseCase(instance()) }
    bindSingleton<UpdateRoomUseCase> { UpdateRoomUseCase(instance()) }
    bindSingleton<DeleteRoomUseCase> { DeleteRoomUseCase(instance()) }

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


//    bindSingleton<TechnologyTreeUseCases> {
//        TechnologyTreeUseCases(
//            getTreeForCourse = GetTreeForCourseUseCase(instance()),
//            createTree = CreateTreeUseCase(instance()),
//            updateTree = UpdateTreeUseCase(instance()),
//            deleteTree = DeleteTreeUseCase(instance()),
//            addNode = AddNodeUseCase(instance()),
//            updateNode = UpdateNodeUseCase(instance()),
//            removeNode = RemoveNodeUseCase(instance()),
//            addConnection = AddConnectionUseCase(instance()),
//            updateConnection = UpdateConnectionUseCase(instance()),
//            removeConnection = RemoveConnectionUseCase(instance())
//        )
//    }
}
