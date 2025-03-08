from fastapi import FastAPI
from app.api.endpoints.routes import router
from app.db.base import Base, engine
from app.core.config import settings

# Создаем таблицы в базе данных
Base.metadata.create_all(bind=engine)

app = FastAPI(title="Auth Service")

# Подключаем основные роутеры
app.include_router(router)

# Подключаем dev-роутеры только в режиме разработки
if settings.ENV == "development":
    from app.api.endpoints.dev_routes import router as dev_router
    app.include_router(dev_router)
