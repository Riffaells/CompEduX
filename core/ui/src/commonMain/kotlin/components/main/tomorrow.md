# Задачи на завтра

## Текущий контекст
На данный момент в проекте реализованы компоненты для работы с курсами:
- `ModernCourseComponents.kt` - компоненты для отображения и редактирования курсов
- `ModernModuleComponents.kt` - компоненты для работы с модулями курса
- `ModernUnifiedCourseContent.kt` - объединенный компонент для работы с курсами

Также создан `MainContent.kt`, который должен служить основным контейнером для всего приложения, но в нём есть проблемы с разрешением ссылок на методы и компоненты.

## Необходимые исправления

### Ошибки в MainContent.kt
1. Неразрешенные ссылки в `MainComponent`:
   - `onCourseSelected`
   - `onCreateCourseClicked`
   - `onBackClicked`

2. Неразрешенные ссылки в `CourseComponent`:
   - `isEditMode`
   - `isModified`
   - `createCourse`
   - `saveCourse` (неприменимый кандидат)
   - `loadCourse`
   - `toggleEditMode`
   - `discardChanges`
   - `addModule`
   - `updateModule`
   - `deleteModule`
   - `moveModuleUp`
   - `moveModuleDown`
   - `addLesson`
   - `editLesson`
   - `deleteLesson`
   - `navigateToModule`

3. Неразрешенная ссылка на класс:
   - `HomeComponent`

### План действий
1. Проверить интерфейсы MainComponent и CourseComponent и добавить недостающие методы
   - Для MainComponent необходимо определить методы:
     ```kotlin
     fun onCourseSelected(courseId: String)
     fun onCreateCourseClicked()
     fun onBackClicked()
     ```

   - Для CourseComponent необходимо реализовать или скорректировать методы:
     ```kotlin
     val isEditMode: Boolean // в state
     val isModified: Boolean // в state
     fun createCourse(course: CourseDomain)
     fun saveCourse(course: CourseDomain)
     fun loadCourse(id: String)
     fun toggleEditMode()
     fun discardChanges()
     fun addModule()
     fun updateModule(module: CourseModuleDomain)
     fun deleteModule(moduleId: String)
     fun moveModuleUp(index: Int)
     fun moveModuleDown(index: Int)
     fun addLesson(moduleId: String)
     fun editLesson(moduleId: String, lessonId: String)
     fun deleteLesson(moduleId: String, lessonId: String)
     fun navigateToModule(moduleId: String)
     ```

   - Также необходимо определить или исправить класс `MainComponent.HomeComponent`

2. Обновить `ModernUnifiedCourseContent.kt` для корректной работы с компонентами, если требуется

## Общие улучшения на будущее
1. Создать полноценный HomeScreen с отображением списка курсов, категорий и поиском
2. Добавить компонент для просмотра отдельного модуля курса
3. Реализовать функциональность для:
   - Управления пользователями и профилями
   - Прогресса прохождения курсов
   - Системы оценок и достижений
4. Улучшить локализацию, добавив больше языков и полноценное управление языками
5. Добавить темную/светлую тему и настраиваемые цвета UI
6. Реализовать улучшенную навигацию с возможностью перехода "назад" без потери состояния
7. Оптимизировать производительность отображения больших списков (LazyColumn с пагинацией)
8. Добавить кеширование данных для оффлайн-режима
9. Реализовать систему уведомлений для курсов
10. Добавить аналитику использования курсов и модулей
