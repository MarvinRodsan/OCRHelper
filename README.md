# OCRHelper

App Android que convierte fotos de documentos en texto, sin conexión a internet.

## Funciones

- Tomar una foto del documento con la cámara
- Seleccionar una imagen de la galería
- Reconocer el texto (OCR) y ordenarlo como se lee, de arriba hacia abajo
- Corregir el texto y el título antes de guardar
- Copiar el texto al portapapeles
- Guardar, buscar, abrir y eliminar documentos
- Los documentos se conservan al cerrar la app

## Cómo abrirlo

1. Descomprime el zip.
2. Android Studio → **File → Open** → elige la carpeta `OCRHelper`.
3. Espera a que termine el *Gradle Sync* (descarga dependencias nuevas).
4. Conecta tu teléfono o abre un emulador y presiona **Run**.

Para correr los tests: clic derecho en `app/src/test` → **Run 'Tests in…'**,
o desde la terminal `./gradlew test`.

## Tecnologías

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · ViewModel + StateFlow ·
Room (KSP) · CameraX · ML Kit Text Recognition · JUnit + coroutines-test

## Arquitectura (MVVM)

```
Pantalla (Compose)  →  ViewModel  →  Repositorio / OCR  →  Room / ML Kit
```

Las pantallas solo dibujan el estado que les da su ViewModel. Los ViewModels
no conocen Room ni ML Kit: dependen de las interfaces `DocumentRepository` y
`TextExtractor`, y por eso se pueden probar con versiones falsas en memoria.

```
app/src/main/java/com/moises/ocrhelper/
├── MainActivity.kt            Punto de entrada
├── OCRHelperApp.kt            Application + contenedor de dependencias
├── camera/
│   └── CameraScreen.kt        Vista previa, permiso y captura (CameraX)
├── data/
│   ├── DocumentEntity.kt      Tabla "documents"
│   ├── DocumentDao.kt         Consultas (buscar, insertar, eliminar)
│   └── AppDatabase.kt         Base de datos Room
├── ocr/
│   ├── TextExtractor.kt       Interfaz del OCR + implementación con ML Kit
│   └── TextOrdering.kt        Orden de las líneas y título sugerido
├── repository/
│   └── DocumentRepository.kt  Interfaz + implementación con Room
├── viewmodel/
│   ├── HomeViewModel.kt       Lista y búsqueda
│   ├── ResultViewModel.kt     OCR, edición y guardado
│   └── DetailViewModel.kt     Documento guardado
└── ui/
    ├── Navigation.kt          Rutas entre pantallas
    ├── HomeScreen.kt          Inicio: botones, búsqueda y documentos
    ├── ResultScreen.kt        Texto detectado
    ├── DetailScreen.kt        Documento guardado
    ├── Components.kt          Diálogo de eliminar y formato de fecha
    └── theme/Theme.kt         Tema Material 3

app/src/test/                  Tests unitarios (orden del texto y ViewModels)
```

## Pantallas

| Ruta | Pantalla |
|---|---|
| `home` | Escanear, galería, búsqueda y lista de documentos |
| `camera` | Cámara |
| `result/{imageUri}` | Texto detectado, editable, con Copiar y Guardar |
| `document/{documentId}` | Documento guardado, con Copiar y Eliminar |

## Ideas para después

- Recortar la imagen y corregir perspectiva antes del OCR
- Exportar a PDF o compartir el texto
- Extraer campos de recibos (fecha, proveedor, total)
- Tests de interfaz
