package usecase.room

/**
 * Container for all course-related use cases
 * Provides access to all course use cases through a single object
 */
data class RoomsUseCases(
    val getRoom: GetRoomUseCase,
    val getRooms: GetRoomsUseCase,
    val getMyRooms: GetMyRoomsUseCase,
    val createRoom: CreateRoomUseCase,
    val updateRoom: UpdateRoomUseCase,
    val deleteRoom: DeleteRoomUseCase,
)
