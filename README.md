# Proyecto de laboratorio – Ingeniería de Software II (2025.2)

## 👥 Autores

<div style="text-align: center;">
<table border="0" style="border:none;">
  <tr>
    <td align="center" style="border:none;">
      <a href="https://github.com/JM-Ortega">
        <img src="https://images.weserv.nl/?url=github.com/JM-Ortega.png&h=100&w=100&fit=cover&mask=circle" alt="Juan Manuel Ortega Narvaez"/>
        <br />
        <sub><b>Juan Manuel Ortega Narvaez</b></sub>
      </a>
    </td>
    <td align="center" style="border:none;">
      <a href="https://github.com/LauraMolano">
        <img src="https://images.weserv.nl/?url=github.com/LauraMolano.png&h=100&w=100&fit=cover&mask=circle" alt="Laura Isabel Molano Bermúdez"/>
        <br />
        <sub><b>Laura Isabel Molano Bermúdez</b></sub>
      </a>
    </td>
    <td align="center" style="border:none;">
      <a href="https://github.com/MaryuriFernandez">
        <img src="https://images.weserv.nl/?url=github.com/MaryuriFernandez.png&h=100&w=100&fit=cover&mask=circle" alt="Maryuri Fernández Salazar"/>
        <br />
        <sub><b>Maryuri Fernández Salazar</b></sub>
      </a>
    </td>
  </tr>
</table>
</div>

---

## 🎯 Objetivo

Desarrollar una **aplicación de escritorio en Java**, con arquitectura **monolítica en 3 capas** y uso del **micro-patrón MVC**, aplicando principios **SOLID** y patrones de diseño GoF para garantizar la **modificabilidad**.  
La aplicación implementa un **conjunto de historias de usuario de alto valor para el cliente** relacionadas con la **gestión del proceso de trabajo de grado en la FIET**

---

## ✨ Requisitos funcionales del primer corte

1. **Registro de docentes** con datos personales e institucionales, validaciones de contraseña seguras y almacenamiento en base de datos. 
2. **Subida de Formato A por docentes**, incluyendo campos obligatorios y carta de aceptación de empresa en caso de modalidad *Práctica Profesional*.  
3. **Evaluación de Formato A por coordinadores**, con estados *aprobado*, *rechazado* y posibilidad de observaciones; notificación a usuarios (simulada con logger).  
4. **Reintentos de Formato A**: el docente puede subir nuevas versiones tras un rechazo; al tercer intento rechazado, el proyecto se cancela.  
5. **Consulta de estado de proyecto por estudiantes**, mostrando etapas: primera, segunda y tercera evaluación, aceptado o rechazado. 

---

## 🛠 Tecnologías

- **Java 21**  
- **JavaFX con FXML** (UI bajo MVC)  
- **SQLite** como base de datos embebida  
- **Maven** para dependencias y build  
- **JUnit 5** para pruebas unitarias de dominio y servicios  
- **Argon2id** para hashing de contraseñas  
- **GitHub** para control de versiones y flujo colaborativo (branches feature, pull requests, merges verificados)  

---

## 🏗 Arquitectura y diseño

- **Arquitectura monolítica en 3 capas**:  
  - **Presentación** (UI con JavaFX)  
  - **Dominio** (entidades, servicios, lógica de negocio)  
  - **Acceso a datos** (repositorios SQLite, interfaces desacopladas)  

- **Micro-patrón MVC** aplicado en las vistas de cada rol.  
- **Principio DIP**: servicios dependen de interfaces, no de implementaciones concretas.  
- **Patrón Observer**: usado en el módulo de estadísticas y notificaciones.  
- **Otros patrones GoF** (Factory, Singleton) aplicados en casos específicos para garantizar extensibilidad.  

---

## ▶️ Ejecución

1. Clonar el repositorio:  
```

git clone [https://github.com/JM-Ortega/labSII_MVC_Project.git](https://github.com/JM-Ortega/labSII_MVC_Project.git)

```
2. Abrir en **IntelliJ IDEA** o **NetBeans**.  
3. Ejecutar con Maven:  
```

mvn clean javafx:run

```
4. La base de datos `app.db` se genera automáticamente en el directorio raíz.  

---

## 📚 Contexto académico

- Proyecto desarrollado para la asignatura de **Ingeniería de Software II (teoría y laboratorio).**
- **Universidad**: Universidad del Cauca – FIET  
- **Periodo**: 2025.2  
- **Entrega**: Primer corte – Proyecto de Gestión del Proceso de Trabajo de Grado.
