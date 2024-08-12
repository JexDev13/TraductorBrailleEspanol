# Manual de Instalación - Brailingo

## Introducción

Este manual proporciona instrucciones detalladas para instalar y configurar el software de traducción de braille a Español y viceversa nombrado brailingo. El software está diseñado para mejorar la accesibilidad mediante la producción de señalética Braille y la traducción de textos en entornos de escritura Braille manual.

### Propósito del Manual de Instalación

El propósito de este manual es guiar a los usuarios a través del proceso de instalación del software en sus sistemas, asegurando una configuración adecuada para su correcto funcionamiento.

## Requisitos del Sistema

### Hardware Necesario

- Procesador: Compatible con x86 o superior
- Memoria RAM: Mínimo 2 GB
- Espacio en Disco Duro: Mínimo 200 MB disponibles

### Sistema Operativo

- Windows 10 (recomendado)
- Otros sistemas operativos compatibles con Java y Apache Maven

### Software Adicional Requerido

- Java Development Kit (JDK) 17 o superior
- Apache Maven 3.8+ para gestión de dependencias

## Instalación de Dependencias Adicionales
Para que el proyecto funcione correctamente, es necesario instalar algunas dependencias de Python. Ejecuta las siguientes sentencias en la terminal o línea de comandos:

```
pip install SpeechRecognition
pip install pyaudio
pip install pyttsx3
```

## Proceso de Instalación (Fork o Clon del Proyecto)

Para aquellos que deseen trabajar con el proyecto desde un repositorio local:

## Proceso de Instalación (Clonar el Repositorio con GitHub Desktop)

Si prefieres utilizar GitHub Desktop para clonar el repositorio y trabajar desde una interfaz gráfica, sigue estos pasos:

1. **Clonar el Repositorio:**

   - Abre GitHub Desktop en tu computadora.
   - Ve a `File` (Archivo) > `Clone Repository` (Clonar repositorio).
   - Selecciona el repositorio que deseas clonar desde la lista de tus repositorios de GitHub o ingresa la URL del repositorio manualmente.
   - Elige la ubicación en tu disco duro donde deseas clonar el repositorio.
   - Haz click en `Clone` (Clonar) para iniciar el proceso de clonación.

2. **Configuración y Compilación:**

   - Una vez clonado, abre NetBeans IDE en tu computadora.
   - Importa el proyecto clonado a NetBeans:
     - Ve a `File` (Archivo) > `Open Project` (Abrir Proyecto).
     - Navega hasta la ubicación donde clonaste el repositorio y selecciona el archivo de proyecto de NetBeans (`pom.xml`).
     - Haz click en `Open Project` (Abrir Proyecto) para cargarlo en NetBeans.

   - Configura las variables de entorno según se documenta en la carpeta `documentacion`.

3. **Compilación y Ejecución:**

   - Ejecuta las tareas de Maven `Clean`

   - Ejecuta el proyecto con el archivo JAR generado.

Este método utiliza GitHub Desktop para el proceso de clonación del repositorio y NetBeans para la configuración inicial y desarrollo del proyecto. Asegúrate de consultar la documentación dentro del repositorio para obtener detalles adicionales sobre la configuración y uso del software.

   - Corre el proyecto de manera local

Este método proporciona una forma conveniente de clonar y trabajar con el repositorio utilizando la interfaz gráfica de GitHub Desktop. Asegúrate de consultar la documentación dentro del repositorio para obtener detalles adicionales sobre la configuración y uso del software.


## Finalización e Indicaciones Posteriores a la Instalación

Una vez completada la instalación, verifica la configuración inicial y asegúrate de que el software funcione correctamente para las necesidades específicas de traducción Braille y gestión de señalética.

## Adicionales

Asegúrate de consultar la carpeta `documentacion` del repositorio para obtener información adicional sobre la configuración y uso avanzado del software.

## Glosario

- **JAR**: Archivo ejecutable Java.
- **JDK**: Java Development Kit.
- **Git**: Sistema de control de versiones.
- **Maven**: Herramienta de gestión de proyectos Java y sus dependencias.

Este manual proporciona una guía básica para comenzar con el software de traducción Braille. Para soporte adicional o preguntas, consulta la documentación o contacta al equipo de desarrollo.
