package repository.mapper

/**
 * Интерфейс для преобразования ошибок API в доменные ошибки приложения
 */
interface ErrorMapper {
    /**
     * Преобразует ошибку API в доменную ошибку приложения
     * @param errorCode код ошибки
     * @param message сообщение об ошибке
     * @param details детали ошибки (опционально)
     * @return объект доменной ошибки приложения
     */
    fun mapToAppError(errorCode: Int, message: String?, details: String?): Any
}
