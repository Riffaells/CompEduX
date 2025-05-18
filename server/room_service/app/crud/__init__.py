"""
CRUD operations for room service
"""

from app.crud.room import RoomCRUD, RoomParticipantCRUD, RoomProgressCRUD

# Создаем экземпляры для удобного использования
room_crud = RoomCRUD()
room_participant_crud = RoomParticipantCRUD()
room_progress_crud = RoomProgressCRUD()

__all__ = [
    "room_crud",
    "room_participant_crud", 
    "room_progress_crud"
]
