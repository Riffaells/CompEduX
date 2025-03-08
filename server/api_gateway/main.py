from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import JSONResponse
import httpx
from app.core.config import settings

app = FastAPI(title="API Gateway")

@app.api_route("/{path:path}", methods=["GET", "POST", "PUT", "DELETE"])
async def gateway(request: Request, path: str):
    # Определяем, к какому сервису направить запрос
    target_service_url = None

    if path.startswith("auth") or path.startswith("users") or path.startswith("token"):
        target_service_url = settings.AUTH_SERVICE_URL
    elif path.startswith("rooms") or path.startswith("invitations"):
        target_service_url = settings.ROOM_SERVICE_URL
    elif path.startswith("competitions"):
        target_service_url = settings.COMPETITION_SERVICE_URL
    elif path.startswith("achievements"):
        target_service_url = settings.ACHIEVEMENT_SERVICE_URL
    else:
        raise HTTPException(status_code=404, detail="Service not found")

    # Получаем метод запроса и заголовки
    method = request.method
    headers = dict(request.headers)

    # Получаем параметры запроса
    params = dict(request.query_params)

    # Получаем тело запроса
    body = await request.body()

    # Формируем URL для запроса к микросервису
    url = f"{target_service_url}/{path}"

    # Отправляем запрос к соответствующему микросервису
    async with httpx.AsyncClient() as client:
        response = await client.request(
            method=method,
            url=url,
            params=params,
            headers=headers,
            content=body
        )

    # Возвращаем ответ от микросервиса
    return JSONResponse(
        content=response.json() if response.content else None,
        status_code=response.status_code,
        headers=dict(response.headers)
    )

@app.get("/health")
def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=settings.API_GATEWAY_PORT)
