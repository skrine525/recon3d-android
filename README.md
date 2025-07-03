# Diplom3D

Android-приложение для 3D-реконструкций.

## Требования

- **Android Studio** (рекомендуется последняя версия)
- **JDK 17** (или версия, требуемая вашим Android SDK)
- **Android SDK** (API уровень, указанный в проекте)
- **Интернет** для загрузки зависимостей

## Сборка и запуск

1. **Клонируйте репозиторий:**
   ```sh
   git clone <URL-репозитория>
   cd Diplom3D
   ```

2. **Откройте проект в Android Studio:**
   - File → Open → выберите папку `Diplom3D`.

3. **Синхронизируйте Gradle:**
   - Android Studio автоматически предложит синхронизировать проект. Если нет — нажмите "Sync Now" в верхней части экрана или используйте меню File → Sync Project with Gradle Files.

4. **Соберите проект:**
   - Через Android Studio: Build → Make Project (или кнопка "Run" для сборки и запуска на устройстве/эмуляторе).
   - Через терминал:
     ```sh
     ./gradlew assembleDebug
     ```
     или для релизной сборки:
     ```sh
     ./gradlew assembleRelease
     ```

5. **Запустите приложение:**
   - Подключите Android-устройство или запустите эмулятор.
   - Нажмите "Run" (зелёная стрелка) в Android Studio или используйте:
     ```sh
     ./gradlew installDebug
     ```

## Структура проекта

- `app/src/main/java/ru/dvfu/diplom3d/` — основной код приложения
- `app/src/main/res/` — ресурсы (layout, drawable, values и т.д.)
- `app/build.gradle.kts` — настройки Gradle для модуля приложения
- `build.gradle.kts`, `settings.gradle.kts` — корневые настройки Gradle

## Примечания

- Для релизной сборки потребуется настроить подпись APK (см. [официальную документацию](https://developer.android.com/studio/publish/app-signing)).
- Если возникнут ошибки с зависимостями, убедитесь, что у вас актуальные репозитории в `build.gradle.kts` и стабильное интернет-соединение. 