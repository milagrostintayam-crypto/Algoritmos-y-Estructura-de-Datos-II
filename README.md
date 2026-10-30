# Sistema básico de registro de hospedaje

Proyecto Java Swing para administrar un hospedaje pequeño. La interfaz se construye desde `frmPrin.java`, por lo que no necesita el diseñador visual de IntelliJ ni archivos `.form`.

## Funciones incluidas

- Registrar el ingreso inmediato de un huésped y asignarle una habitación.
- Crear reservas para fechas futuras.
- Asignar habitaciones automáticamente a las reservas pendientes.
- Registrar llegada, pagos, cancelaciones y salida.
- Consultar el estado de las habitaciones, marcarlas limpias o bloquearlas.
- Registrar vehículos en la cochera y anotar su retiro.
- Guardar los datos en `datos/hospedaje.dat` al realizar cambios y al cerrar el programa.

## Requisitos

- JDK 21.
- IntelliJ IDEA o cualquier IDE que permita ejecutar Java.

## Cómo abrir y ejecutar en IntelliJ

1. Descomprime `prySistHospUI.zip` en una carpeta.
2. En IntelliJ selecciona **Open** y abre la carpeta `prySistHospUI`.
3. Si pregunta por el proyecto Maven, selecciona **Load**. El proyecto no usa librerías externas.
4. Comprueba que el SDK del proyecto sea Java 21: **File > Project Structure > Project SDK**.
5. Abre `src/main/java/Main.java`.
6. Pulsa el triángulo verde junto a `main` y selecciona **Run 'Main.main()'**.
7. Los registros se guardarán automáticamente en la carpeta `datos` creada junto al proyecto.

También se puede compilar desde una terminal en la carpeta del proyecto:

```bash
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out Main
```

En Windows PowerShell se puede compilar desde IntelliJ con el paso 5; así se evita la diferencia de sintaxis de `find` entre sistemas operativos.

## Diseño de la interfaz

La ventana tiene cuatro pestañas:

1. **Recepción:** formulario de DNI, nombres, teléfono, ingreso, salida, tipo de habitación y adelanto. Incluye botones para registrar un ingreso de hoy, crear una reserva o limpiar los campos.
2. **Reservas:** tabla general y botones para asignar habitaciones, registrar llegada, pago, salida o cancelación.
3. **Habitaciones:** tabla de las habitaciones 101–105, tipo, tarifa y estado; permite marcar una habitación limpia o habilitarla/bloquearla.
4. **Cochera:** permite asociar un vehículo con una estadía alojada y registrar su retiro.

Fechas: usar el formato `AAAA-MM-DD HH:mm`, por ejemplo `2026-10-30 14:00`. El DNI debe tener ocho dígitos.

## Estructuras de datos

- **Listas (`ArrayList`)**: se usan para almacenar habitaciones, reservas, cocheras y préstamos, porque el sistema necesita recorrerlos, agregarlos y mostrarlos en tablas.
- **Grafo (`GrafoReservas`)**: cada reserva vigente es un vértice. Se crea una arista entre dos reservas del mismo tipo de habitación cuando sus fechas se cruzan. La asignación automática busca habitaciones compatibles sin dar la misma habitación a reservas que se superponen. El grafo tiene una función concreta; no se usa para tareas que una lista resuelve mejor.

## Reglas simplificadas

- Habitaciones 101–103: baño compartido (S/ 30 por noche).
- Habitaciones 104–105: baño privado (S/ 40 por noche).
- Sábados y domingos: recargo de S/ 10 por noche.
- El sistema de ejemplo guarda los datos en un archivo local; no es una base de datos multiusuario.
- En una instalación real se deben agregar copias de seguridad, control de usuarios y revisión contable de pagos/devoluciones.
