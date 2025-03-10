from fastapi import FastAPI, Depends, HTTPException, status, Request, Response
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from typing import List, Optional
import models, schemas, database
from passlib.context import CryptContext
from jose import JWTError, jwt
from datetime import datetime, timedelta
import os
import httpx
import uuid
from pydantic import BaseModel

# Создаем таблицы в базе данных
models.Base.metadata.create_all(bind=database.engine)

app = FastAPI(title="Auth Service")

# Настройки JWT
SECRET_KEY = os.getenv("SECRET_KEY", "09d25e094faa6ca2556c818166b7a9563b93f7099f6f0f4caa6cf63b88e8d3e7")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "30"))
REFRESH_TOKEN_EXPIRE_DAYS = int(os.getenv("REFRESH_TOKEN_EXPIRE_DAYS", "7"))

# OAuth настройки
GOOGLE_CLIENT_ID = os.getenv("GOOGLE_CLIENT_ID", "")
GOOGLE_CLIENT_SECRET = os.getenv("GOOGLE_CLIENT_SECRET", "")
GOOGLE_REDIRECT_URI = os.getenv("GOOGLE_REDIRECT_URI", "http://localhost:3000/auth/google/callback")

GITHUB_CLIENT_ID = os.getenv("GITHUB_CLIENT_ID", "")
GITHUB_CLIENT_SECRET = os.getenv("GITHUB_CLIENT_SECRET", "")
GITHUB_REDIRECT_URI = os.getenv("GITHUB_REDIRECT_URI", "http://localhost:3000/auth/github/callback")

# Настройки безопасности
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

# Функции для работы с паролями и токенами
def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password):
    return pwd_context.hash(password)

def create_access_token(data: dict, expires_delta: timedelta = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def create_refresh_token(data: dict, expires_delta: timedelta = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

# Функции для работы с пользователями
def get_user(db: Session, username: str):
    return db.query(models.User).filter(models.User.username == username).first()

def get_user_by_id(db: Session, user_id: int):
    return db.query(models.User).filter(models.User.id == user_id).first()

def get_user_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email).first()

def get_users(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.User).offset(skip).limit(limit).all()

def create_user(db: Session, user: schemas.UserCreate):
    # Создаем пользователя
    db_user = models.User(
        username=user.username,
        email=user.email,
        hashed_password=get_password_hash(user.password) if user.password else None,
        role=models.UserRole.USER
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)

    # Если есть данные о провайдере авторизации, добавляем их
    if user.auth_provider:
        auth_provider = models.AuthProvider(
            user_id=db_user.id,
            provider=user.auth_provider.provider,
            provider_id=user.auth_provider.provider_id
        )
        db.add(auth_provider)
        db.commit()

    return db_user

def get_user_by_provider(db: Session, provider: models.AuthType, provider_id: str):
    auth_provider = db.query(models.AuthProvider).filter(
        models.AuthProvider.provider == provider,
        models.AuthProvider.provider_id == provider_id
    ).first()

    if auth_provider:
        return get_user_by_id(db, auth_provider.user_id)
    return None

# Зависимость для получения текущего пользователя
async def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(database.get_db)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        user_id: int = payload.get("user_id")
        role: str = payload.get("role")

        if username is None or user_id is None:
            raise credentials_exception

        token_data = schemas.TokenData(username=username, user_id=user_id, role=role)
    except JWTError:
        raise credentials_exception

    user = get_user_by_id(db, user_id=token_data.user_id)
    if user is None:
        raise credentials_exception
    return user

# Проверка роли пользователя
def check_admin_role(current_user: models.User = Depends(get_current_user)):
    if current_user.role != models.UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    return current_user

def check_moderator_role(current_user: models.User = Depends(get_current_user)):
    if current_user.role not in [models.UserRole.ADMIN, models.UserRole.MODERATOR]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    return current_user

# Модели для OAuth
class OAuthLoginRequest(BaseModel):
    code: str
    redirect_uri: Optional[str] = None

# Эндпоинты для аутентификации
@app.post("/token", response_model=schemas.Token)
async def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(database.get_db)):
    user = get_user(db, form_data.username)
    if not user or not user.hashed_password or not verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Создаем токены
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    refresh_token_expires = timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS)

    access_token = create_access_token(
        data={"sub": user.username, "user_id": user.id, "role": user.role.value},
        expires_delta=access_token_expires
    )

    refresh_token = create_refresh_token(
        data={"sub": user.username, "user_id": user.id, "type": "refresh"},
        expires_delta=refresh_token_expires
    )

    return {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "bearer"
    }

@app.post("/refresh-token", response_model=schemas.Token)
async def refresh_access_token(refresh_token: str, db: Session = Depends(database.get_db)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate refresh token",
        headers={"WWW-Authenticate": "Bearer"},
    )

    try:
        payload = jwt.decode(refresh_token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        user_id: int = payload.get("user_id")
        token_type: str = payload.get("type")

        if username is None or user_id is None or token_type != "refresh":
            raise credentials_exception
    except JWTError:
        raise credentials_exception

    user = get_user_by_id(db, user_id=user_id)
    if user is None:
        raise credentials_exception

    # Создаем новый access token
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": user.username, "user_id": user.id, "role": user.role.value},
        expires_delta=access_token_expires
    )

    return {
        "access_token": access_token,
        "refresh_token": refresh_token,  # Возвращаем тот же refresh token
        "token_type": "bearer"
    }

# OAuth эндпоинты
@app.post("/auth/google", response_model=schemas.Token)
async def google_auth(request: OAuthLoginRequest, db: Session = Depends(database.get_db)):
    # Обмен кода на токен
    token_url = "https://oauth2.googleapis.com/token"
    data = {
        "client_id": GOOGLE_CLIENT_ID,
        "client_secret": GOOGLE_CLIENT_SECRET,
        "code": request.code,
        "grant_type": "authorization_code",
        "redirect_uri": request.redirect_uri or GOOGLE_REDIRECT_URI
    }

    async with httpx.AsyncClient() as client:
        response = await client.post(token_url, data=data)
        if response.status_code != 200:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Could not validate Google credentials"
            )

        token_data = response.json()
        access_token = token_data.get("access_token")

        # Получаем информацию о пользователе
        user_info_url = "https://www.googleapis.com/oauth2/v2/userinfo"
        user_response = await client.get(
            user_info_url,
            headers={"Authorization": f"Bearer {access_token}"}
        )

        if user_response.status_code != 200:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Could not get user info from Google"
            )

        user_info = user_response.json()
        google_id = user_info.get("id")
        email = user_info.get("email")
        name = user_info.get("name")
        picture = user_info.get("picture")

        # Проверяем, есть ли пользователь с таким Google ID
        user = get_user_by_provider(db, models.AuthType.GOOGLE, google_id)

        # Если нет, создаем нового пользователя
        if not user:
            # Проверяем, есть ли пользователь с таким email
            user = get_user_by_email(db, email)

            if not user:
                # Создаем нового пользователя
                username = f"user_{uuid.uuid4().hex[:8]}"
                user_create = schemas.UserCreate(
                    username=username,
                    email=email,
                    auth_provider=schemas.AuthProviderCreate(
                        provider=schemas.AuthType.GOOGLE,
                        provider_id=google_id
                    )
                )
                user = create_user(db, user_create)

                # Обновляем аватар
                user.avatar_url = picture
                db.commit()
            else:
                # Добавляем Google как провайдер для существующего пользователя
                auth_provider = models.AuthProvider(
                    user_id=user.id,
                    provider=models.AuthType.GOOGLE,
                    provider_id=google_id
                )
                db.add(auth_provider)
                db.commit()

        # Создаем токены
        access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
        refresh_token_expires = timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS)

        access_token = create_access_token(
            data={"sub": user.username, "user_id": user.id, "role": user.role.value},
            expires_delta=access_token_expires
        )

        refresh_token = create_refresh_token(
            data={"sub": user.username, "user_id": user.id, "type": "refresh"},
            expires_delta=refresh_token_expires
        )

        return {
            "access_token": access_token,
            "refresh_token": refresh_token,
            "token_type": "bearer"
        }

@app.post("/auth/github", response_model=schemas.Token)
async def github_auth(request: OAuthLoginRequest, db: Session = Depends(database.get_db)):
    # Обмен кода на токен
    token_url = "https://github.com/login/oauth/access_token"
    data = {
        "client_id": GITHUB_CLIENT_ID,
        "client_secret": GITHUB_CLIENT_SECRET,
        "code": request.code,
        "redirect_uri": request.redirect_uri or GITHUB_REDIRECT_URI
    }

    async with httpx.AsyncClient() as client:
        headers = {"Accept": "application/json"}
        response = await client.post(token_url, data=data, headers=headers)

        if response.status_code != 200:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Could not validate GitHub credentials"
            )

        token_data = response.json()
        access_token = token_data.get("access_token")

        # Получаем информацию о пользователе
        user_info_url = "https://api.github.com/user"
        user_response = await client.get(
            user_info_url,
            headers={
                "Authorization": f"token {access_token}",
                "Accept": "application/json"
            }
        )

        if user_response.status_code != 200:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Could not get user info from GitHub"
            )

        user_info = user_response.json()
        github_id = str(user_info.get("id"))
        name = user_info.get("name")
        avatar_url = user_info.get("avatar_url")

        # Получаем email (может быть приватным)
        email_url = "https://api.github.com/user/emails"
        email_response = await client.get(
            email_url,
            headers={
                "Authorization": f"token {access_token}",
                "Accept": "application/json"
            }
        )

        email = None
        if email_response.status_code == 200:
            emails = email_response.json()
            for email_obj in emails:
                if email_obj.get("primary") and email_obj.get("verified"):
                    email = email_obj.get("email")
                    break

        # Проверяем, есть ли пользователь с таким GitHub ID
        user = get_user_by_provider(db, models.AuthType.GITHUB, github_id)

        # Если нет, создаем нового пользователя
        if not user:
            # Проверяем, есть ли пользователь с таким email (если email доступен)
            if email:
                user = get_user_by_email(db, email)

            if not user:
                # Создаем нового пользователя
                username = f"user_{uuid.uuid4().hex[:8]}"
                user_create = schemas.UserCreate(
                    username=username,
                    email=email,
                    auth_provider=schemas.AuthProviderCreate(
                        provider=schemas.AuthType.GITHUB,
                        provider_id=github_id
                    )
                )
                user = create_user(db, user_create)

                # Обновляем аватар
                user.avatar_url = avatar_url
                db.commit()
            else:
                # Добавляем GitHub как провайдер для существующего пользователя
                auth_provider = models.AuthProvider(
                    user_id=user.id,
                    provider=models.AuthType.GITHUB,
                    provider_id=github_id
                )
                db.add(auth_provider)
                db.commit()

        # Создаем токены
        access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
        refresh_token_expires = timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS)

        access_token = create_access_token(
            data={"sub": user.username, "user_id": user.id, "role": user.role.value},
            expires_delta=access_token_expires
        )

        refresh_token = create_refresh_token(
            data={"sub": user.username, "user_id": user.id, "type": "refresh"},
            expires_delta=refresh_token_expires
        )

        return {
            "access_token": access_token,
            "refresh_token": refresh_token,
            "token_type": "bearer"
        }

# Эндпоинты для пользователей
@app.post("/users/", response_model=schemas.User)
def create_new_user(user: schemas.UserCreate, db: Session = Depends(database.get_db)):
    # Проверяем, есть ли пользователь с таким email
    if user.email:
    db_user = get_user_by_email(db, email=user.email)
    if db_user:
        raise HTTPException(status_code=400, detail="Email already registered")

    # Проверяем, есть ли пользователь с таким username
    db_user = get_user(db, username=user.username)
    if db_user:
        raise HTTPException(status_code=400, detail="Username already registered")

    # Проверяем, указан ли пароль или провайдер
    if not user.password and not user.auth_provider:
        raise HTTPException(status_code=400, detail="Password or auth provider is required")

    return create_user(db=db, user=user)

@app.get("/users/", response_model=List[schemas.User])
def read_users(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(check_moderator_role)
):
    users = get_users(db, skip=skip, limit=limit)
    return users

@app.get("/users/me/", response_model=schemas.User)
async def read_users_me(current_user: models.User = Depends(get_current_user)):
    return current_user

@app.put("/users/me/", response_model=schemas.User)
async def update_user(
    user_update: schemas.UserUpdate,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(database.get_db)
):
    # Обновляем поля пользователя
    if user_update.username is not None:
        # Проверяем, не занято ли имя пользователя
        existing_user = get_user(db, username=user_update.username)
        if existing_user and existing_user.id != current_user.id:
            raise HTTPException(status_code=400, detail="Username already taken")
        current_user.username = user_update.username

    if user_update.email is not None:
        # Проверяем, не занят ли email
        existing_user = get_user_by_email(db, email=user_update.email)
        if existing_user and existing_user.id != current_user.id:
            raise HTTPException(status_code=400, detail="Email already registered")
        current_user.email = user_update.email

    if user_update.password is not None:
        current_user.hashed_password = get_password_hash(user_update.password)

    if user_update.avatar_url is not None:
        current_user.avatar_url = user_update.avatar_url

    db.commit()
    db.refresh(current_user)

    return current_user

@app.put("/users/{user_id}/role", response_model=schemas.User)
async def update_user_role(
    user_id: int,
    role: schemas.UserRole,
    current_user: models.User = Depends(check_admin_role),
    db: Session = Depends(database.get_db)
):
    # Получаем пользователя
    user = get_user_by_id(db, user_id=user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Обновляем роль
    user.role = role
    db.commit()
    db.refresh(user)

    return user

# Эндпоинт для проверки токена
@app.get("/verify-token")
async def verify_token(token: str, db: Session = Depends(database.get_db)):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        user_id: int = payload.get("user_id")
        role: str = payload.get("role")

        if username is None or user_id is None:
            return {"valid": False}

        user = get_user_by_id(db, user_id=user_id)
        if user is None:
            return {"valid": False}

        return {
            "valid": True,
            "user_id": user_id,
            "username": username,
            "role": role
        }
    except JWTError:
        return {"valid": False}

# Health check endpoint
@app.get("/health")
def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
