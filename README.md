# DentalApp Android

Aplicación Android para simulaciones de diseño de sonrisa, con integración a Supabase (auth, almacenamiento, Postgres) y opcionalmente Gemini AI.

## Configuración

- **Supabase**: `SUPABASE_URL` y `SUPABASE_KEY` en `local.properties`
- **Gemini (opcional)**: `GEMINI_API_KEY` en `local.properties` para análisis con IA

---

## Recomendación para la Demo

Para probar la app y que se vea **visualmente atractiva** (con un cambio claro en el slider Antes/Después):

| Modo | API Key | Resultado visual |
|------|---------|------------------|
| **Mock** (sin API Key válida) | Vacía o placeholder | ✅ Se muestra una imagen de ejemplo en "Después" — **ideal para demos** porque el slider muestra un contraste visual real (tu foto vs. representación mejorada) |
| **Con Gemini** | Válida | ⚠️ Solo se genera la **descripción de texto** con IA. La foto no cambia; antes y después son la misma imagen. La IA analiza la sonrisa y describe el tratamiento, pero no genera una nueva imagen. |

**Conclusión**: Para una demo impactante, usa el **modo Mock** (deja vacío o con placeholder `GEMINI_API_KEY` en `local.properties`). Para producción con análisis de texto por IA, configura la API Key real.

---

## Estructura

- `data/` — Modelos, repositorios, servicios remotos
- `domain/` — Interfaces de repositorios
- `presentation/` — ViewModels por pantalla
- `ui/` — Compose screens, navegación, temas
