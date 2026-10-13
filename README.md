# EjemploConexionHttp

Taller **Integración con HTTP** — Desarrollo de aplicaciones para dispositivos móviles (Unicolombo).
App Android en Java que se conecta por HTTP a un servicio PHP + MySQL (MariaDB) y tiene tres pantallas:

| Pantalla | Qué hace | Acción del servicio |
|---|---|---|
| `PantallaInicio` | Inicio de sesión; botón "Regístrate" | `login` |
| `PantallaCrudUsuario` | Registra un usuario nuevo, o modifica los datos del que inició sesión | `Agregar` / `editar` |
| `PantallaListado` | Lista todos los usuarios; botón "Edita tus datos" | `listar` |

```
App Android --POST (accion, email, psw, nombre)--> Apache + PHP (operacion.php) --> MariaDB (crudphpjson.Usuarios)
App Android <------------------------- JSON ------------------------------------
```

## Cómo probarla

1. En XAMPP, encender **Apache** y **MySQL**.
2. Copiar `servidor/crudphpjson` a `C:\xampp\htdocs\` e importar `servidor/crear_base_de_datos.sql` (phpMyAdmin → Importar).
3. Comprobar en el navegador: `http://localhost/crudphpjson/crud/operacion.php?accion=listar` → `{"mensaje":"No hay Usuarios"}`.
4. Abrir este proyecto en Android Studio y ejecutarlo en un emulador (usa `10.0.2.2`, el alias con el que el emulador llega al PC).

**Celular real:** poner la IP del PC en `url_servidor` (`res/values/strings.xml`) y agregarla en `res/xml/network_security_config.xml`.

## Estructura

```
joseblanco.ejemploconexionhttp
├── vistas          PantallaInicio, PantallaCrudUsuario, PantallaListado
├── datos           Usuario (Serializable), Mensaje
└── controladores   ConexionHttpPostServer (HTTP), SegundoPlano (hilo secundario)
```

## Diferencias con la guía y por qué

| La guía | Aquí | Motivo |
|---|---|---|
| Apache HttpClient 3.x (`.jar` sueltos) | `HttpURLConnection` | HttpClient 3.x está sin mantenimiento desde 2011; `HttpURLConnection` ya viene en Android y no añade dependencias. |
| Gson con `.jar` sueltos | Gson 2.14.0 por Gradle | Se descarga de Maven Central y queda versionado en `libs.versions.toml`. |
| `AsyncTask` | `ExecutorService` + `Handler` | `AsyncTask` está obsoleta desde la API 30. |
| `ProgressDialog` | `ProgressBar` en el layout | `ProgressDialog` está obsoleto desde la API 26. |
| HTTP sin restricciones | `network_security_config.xml` solo para `10.0.2.2` | Desde Android 9 el HTTP sin cifrar viene bloqueado; se permite solo hacia el emulador. |
| `static Usuario fulanito` como sesión | El `Usuario` viaja por `Intent` (`Serializable`) | Un estado estático queda con datos viejos al volver al inicio. |
| Password visible en el login | `textPassword` | No mostrar la clave en pantalla. |
| `Thread.sleep(2000)` en el hilo principal | Se navega de inmediato | Bloquear el hilo principal congela la pantalla. |
| Toast desde el hilo de red | Todo aviso se muestra en el hilo principal | Un Toast desde otro hilo cierra la app. |
| URL repetida 3 veces | Una sola, en `strings.xml` | Cambiarla en un solo lugar. |
| Permisos `INTERNET` y `ACCESS_NETWORK_STATE` | Solo `INTERNET` | No se consulta el estado de la red. |

Correcciones al servidor PHP de la guía (`servidor/`):

- `throw new Exception($arreglo)` da `TypeError` en PHP 8 → se lanza el texto del error.
- `guardar()` respondía `"OK)"` (con paréntesis) y la app esperaba `"OK"`.
- `Agregar` con un correo que ya existía **sobrescribía** esa cuenta; ahora responde `El usuario ya existe`.
- Las consultas arman el SQL pegando el texto (una comilla simple las rompe y abre la puerta a inyección SQL); para este ejercicio local se dejó como en la guía, pero habría que pasar a consultas preparadas antes de publicarlo.

## Pruebas

- `./gradlew testDebugUnitTest` — 8 pruebas JUnit: la clase HTTP contra un servidor falso (UTF-8, `&`, `=`, `+`, códigos de error, respuesta vacía) y Gson contra el JSON real del servicio.
- Prueba de extremo a extremo en el emulador (Android 15) contra el servidor real: validaciones, login fallido, registro, correo repetido, login, listado y edición.
