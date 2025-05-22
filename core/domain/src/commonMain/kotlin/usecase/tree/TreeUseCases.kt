package usecase.tree

/**
 * Container for all technology tree use cases
 */
data class TreeUseCases(
    val getTreeForCourse: GetTreeForCourseUseCase,
    val createTree: CreateTreeUseCase,
    val updateTree: UpdateTreeUseCase,
    val deleteTree: DeleteTreeUseCase,
    val addNode: AddNodeUseCase,
    val updateNode: UpdateNodeUseCase,
    val removeNode: RemoveNodeUseCase,
    val addConnection: AddConnectionUseCase,
    val updateConnection: UpdateConnectionUseCase,
    val removeConnection: RemoveConnectionUseCase
) 