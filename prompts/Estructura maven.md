# Prompt 01: estructura Maven multimódulo

- **Autor:** Ilyes (módulos WM_WS_M, WM_WS_E, WM_FO y shared)
- **Fecha:** 6 de octubre de 2026
- **Herramienta:** Claude (Anthropic)
- **Tipo:** prompt consolidado. La sesión real se hizo en varios mensajes
  iterativos; aquí se recoge unificado en un único prompt, que es la forma
  recomendada de plantearlo.

## Contexto personal

Mi experiencia con Java se limita a proyectos de programacion 3 en eclipse. No
había usado Maven ni proyectos multimódulo, Por eso usé la IA como apoyo de aprendizaje y
contrasté sus respuestas con la documentación oficial.

## Prompt consolidado

```text
# ROL
Actúa como tutor de Java y Maven para un estudiante de 3.º de Ingeniería
Informática que conoce Java básico en Eclipse pero no Maven.

# CONTEXTO
Hago la práctica "WaterManagement" de Sistemas Distribuidos (enunciado
adjunto). Trabajo en pareja. Mi parte son tres ejecutables con nombres
obligatorios: WM_WS_M (Monitor), WM_WS_E (Engine) y WM_FO (Field Operator),
más un módulo común "shared" (tramas STX/ETX/LRC, constantes, mensajes
Kafka). Mi compañero hace WM_Central.
- Comunicación: sockets (Monitor-Engine, Monitor-Central) y Kafka
  (Engine/FO con Central).
- Despliegue final: Docker obligatorio, repositorio en GitHub.

# ENTORNO
- Linux Mint, VSCodium (instalado desde apt), JDK 21, Maven y Docker.
- Raíz del repo: SD-Water-Management.
- Estructura deseada:
  shared/, WM_FO/, WM_WS/ (solo agregador) con WM_WS_E/ y WM_WS_M/.
  WM_Central lo gestiona mi compañero.

# OBJETIVO
Montar un proyecto Maven multimódulo con un POM padre y cuatro módulos
hijos, donde cada ejecutable genere un jar con su nombre obligatorio
(WM_WS_M.jar, WM_WS_E.jar, WM_FO.jar).

# RESTRICCIONES
- No quiero código de lógica de la aplicación (sockets, hilos, Kafka):
  esa parte la programo yo.
- Sí acepto configuración de build (pom.xml), explicada.
- Usa el método estándar de la documentación oficial: generar los módulos
  con maven-archetype-quickstart (Maven in 5 Minutes), un módulo cada vez,
  compilando tras cada uno.
- Nombra siempre los comandos exactos y asume que no tengo nada
  instalado ni creado, sin darme alternativas.
- Cita fuentes oficiales (maven.apache.org, mvnrepository.com) para que
  pueda comprobar cada paso.

# PASOS QUE NECESITO
1. POM padre: packaging pom, modules, properties (Java 17),
   dependencyManagement (JUnit, Kafka, Gson, slf4j) y pluginManagement.
2. Generación de shared, WM_FO, WM_WS (agregador), WM_WS_E y WM_WS_M
   con el arquetipo, y la limpieza posterior (.mvn, App.java, AppTest.java,
   renombrar App a Main).
3. POM de cada módulo: <parent> con relativePath, dependencia de shared,
   finalName y maven-shade-plugin con mainClass.
4. Comprobación: mvn clean package desde la raíz, Reactor Summary con
   todos los módulos y java -jar de cada jar.
5. Errores típicos de POM y cómo diagnosticarlos.

# FORMATO DE RESPUESTA
Pasos numerados, cada uno con: comando, qué debo ver si va bien, y la
fuente oficial. Explica el porqué de cada bloque del POM para que pueda
defenderlo ante el profesor.
```

## Cómo se llegó a este prompt

1. Pedí una organización del trabajo en pareja a partir del enunciado.
2. Elegí Java y pedí que no me diera código de lógica.
3. Resolví problemas de entorno (VSCodium Flatpak aislaba la terminal).
4. Escribí los POM a mano con ayuda y fallaron (`<properties>` ausente,
   `</dependencyManagement>` sin abrir, carpetas `my-app` anidadas).
5. Decidí empezar de cero con el arquetipo estándar y compilar módulo a
   módulo.
6. El prompt anterior unifica todo lo aprendido en esas iteraciones.

## Fuentes de contraste

- https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html
- https://maven.apache.org/guides/introduction/introduction-to-the-pom.html
- https://maven.apache.org/guides/mini/guide-multiple-modules.html
- https://maven.apache.org/guides/introduction/introduction-to-archetypes.html
- https://maven.apache.org/plugins/maven-shade-plugin/examples/executable-jar.html
- https://mvnrepository.com

## Qué aportó la IA y qué hice yo

- **IA:** planificación, explicación de conceptos, diagnóstico de errores
  y la configuración de build de los pom.xml (padre y módulos).
- **Yo:** montaje del entorno, ejecución de los comandos, verificación con
  `mvn clean package`, limpieza del proyecto y toda la lógica de la
  aplicación, que registraré en los ficheros siguientes.