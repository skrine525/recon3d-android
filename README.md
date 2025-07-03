# Diplom3D

## Требования

- **Android Studio** (рекомендуется последняя версия)
- **JDK 17** (или версия, требуемая вашим Android SDK)
- **Android SDK** (API 35)

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