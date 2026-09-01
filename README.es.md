# KStack

[![CI](https://img.shields.io/github/actions/workflow/status/antmordel/karoo-kstack/ci.yml?branch=main)](https://github.com/antmordel/karoo-kstack/actions/workflows/ci.yml)
[![Descargas](https://img.shields.io/github/downloads/antmordel/karoo-kstack/app-release.apk)](https://github.com/antmordel/karoo-kstack/releases)
[![Licencia](https://img.shields.io/github/license/antmordel/karoo-kstack)](LICENSE)

[English](README.md) · **Español**

Campos de datos apilados al estilo Garmin para [Hammerhead Karoo](https://www.hammerhead.io/),
construidos sobre el SDK oficial [karoo-ext](https://github.com/hammerheadnav/karoo-ext).

KStack dibuja el valor actual de una métrica en grande con sus acumulados de la ruta pequeños
debajo, así que un campo lleva tres números donde uno de fábrica lleva uno.

Compatible con Karoo 2 y Karoo 3.

![Seis campos de KStack en una página de datos del Karoo](fields.png)

[Campos](#campos) · [Colores de zona y ajustes](#colores-de-zona-y-ajustes) ·
[Instalación](#instalación) · [Actualizar](#actualizar) ·
[Compilar desde el código](#compilar-desde-el-código)

La aplicación está en inglés, así que los nombres de los campos aparecen tal cual en el selector
del Karoo.

## Campos

| Campo | En grande | Debajo |
|---|---|---|
| HR Stack | Pulso | media, máximo |
| HR% Stack | Pulso como porcentaje de tu pulso máximo | media, máximo |
| Speed Stack | Velocidad | media, máximo |
| Power Stack | Potencia | NP, media |
| Cadence Stack | Cadencia | media, máximo |
| Time Stack | Tiempo de vuelta | tiempo total, paradas incluidas |

Los seis son campos gráficos y escalan su texto al tamaño de casilla en el que los coloques.

Los acumulados son los tipos de dato `AVERAGE_*` y `MAX_*` del propio Karoo, así que coinciden
exactamente con los campos de fábrica y siguen su mismo comportamiento al pausar y al reiniciar.
HR% divide esos mismos valores de pulso entre el pulso máximo de tu perfil de usuario del Karoo; si
no tienes máximo configurado, esas filas se quedan vacías.

La velocidad sigue la preferencia de unidades de tu perfil, en km/h o mph. Cada fila se resuelve por
su cuenta, así que un sensor desconectado muestra `--` en su fila mientras las demás siguen
actualizándose.

## Colores de zona y ajustes

Abre KStack desde el menú principal para llegar a sus ajustes. Cada campo tiene los suyos:

- **Color de zona**: apagado, el icono de la métrica en el color de tu zona actual, o el fondo
  entero del campo con el texto ajustándose. El pulso y la potencia tienen zonas. La velocidad, la
  cadencia y el tiempo no, así que esos campos no ofrecen elección de color.
- **Valores secundarios**: en paralelo, o apilados uno por fila.

Los cambios se aplican a los campos que ya tengas en una página de datos, sin necesidad de volver a
añadirlos.

## Instalación

### [⬇ Descargar app-release.apk](https://github.com/antmordel/karoo-kstack/releases/latest/download/app-release.apk)

Ese enlace apunta siempre a la última versión, así que es el que conviene compartir o guardar. La
[página de la release](https://github.com/antmordel/karoo-kstack/releases/latest) tiene las notas y
las versiones anteriores.

**Karoo 3**: abre esta página en el navegador del móvil, mantén pulsado el enlace de descarga de
arriba y compártelo con la app Hammerhead Companion, luego pulsa Install en el Karoo. Hammerhead
documenta el proceso en [Companion App Sideloading](https://support.hammerhead.io/hc/en-us/articles/31576497036827-Companion-App-Sideloading).

**Karoo 2**: activa el sideloading ([guía de DC Rainmaker](https://www.dcrainmaker.com/2021/02/how-to-sideload-android-apps-on-your-hammerhead-karoo-1-karoo-2.html))
y ejecuta:

```bash
curl -LO https://github.com/antmordel/karoo-kstack/releases/latest/download/app-release.apk
adb install app-release.apk
```

Abre KStack una vez desde el menú principal después de instalarlo. Los seis campos aparecen
entonces en el selector de campos al editar una página de datos, con los nombres de la tabla de
arriba.

## Actualizar

Mantén pulsado el icono de KStack en el menú principal y elige Update. El Karoo consulta la última
release de este repositorio y la instala en sitio, conservando tus ajustes y los campos que ya
tengas en las páginas de datos.

Esto funciona a partir de la v0.2.3. Una copia instalada desde una release anterior no tiene URL de
actualización que seguir, así que necesita una reinstalación manual para engancharse al mecanismo.

## Compilar desde el código

`karoo-ext` se publica solo en GitHub Packages, que exige autenticación aunque el paquete sea
público. Crea un token de acceso personal con el permiso `read:packages` y ponlo en
`local.properties`:

```properties
gpr.user=tu-usuario-de-github
gpr.key=ghp_tu_token
```

Las variables de entorno `GPR_USER` y `GPR_KEY` también valen. Después:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

`./gradlew assembleRelease` produce un APK sin firmar salvo que estén puestas las variables de
entorno de firma; en [AGENTS.md](AGENTS.md) está cómo se construyen y se firman las releases.

## Créditos

- Construido sobre [karoo-ext](https://github.com/hammerheadnav/karoo-ext) (Apache-2.0)
- La estructura del proyecto y las convenciones de release siguen a [timklge/karoo-headwind](https://github.com/timklge/karoo-headwind)

## Licencia

Apache-2.0. Ver [LICENSE](LICENSE).
