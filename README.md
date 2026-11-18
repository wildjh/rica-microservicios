# rica-api (archivado)

Este proyecto fue dividido en `investigadores-service` y `publicaciones-service`
a partir del Taller de la Lección 5. Ver `../rica-microservicios/`.

Este es el json resultante del uso del endpoint 

GET http://localhost:8083/api/consultas/investigadores/{correo}

{
  "correo": "luis.mora@uptc.edu.co",
  "nombreCompleto": "Luis Mora",
  "estado": "ACTIVO",
  "totalPublicaciones": 2,
  "publicaciones": [
    {
      "id": "6afdbbd38016e8421458506e",
      "titulo": "Redes de colaboracion academica en instituciones publicas LUIS MORA",
      "tipo": "articulo",
      "anio": 2025
    },
    {
      "id": "6afdbbd88016e8421458506f",
      "titulo": "Redes de colaboracion academica en instituciones publicas LUIS MORA 2",
      "tipo": "articulo",
      "anio": 2025
    }
  ]
}

Esta es la evidencia de consultas-service y sus proyecciones

2026-11-18T09:59:22.622-05:00  INFO 38860 --- [consultas-service] [ntainer#1-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : consultas-service-v2: partitions assigned: [investigadores.eliminados-0]
2026-11-18T10:04:21.218-05:00  INFO 38860 --- [consultas-service] [rap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver      : Resolving eureka endpoints via configuration
2026-11-18T10:09:21.229-05:00  INFO 38860 --- [consultas-service] [rap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver      : Resolving eureka endpoints via configuration
2026-11-18T10:11:25.551-05:00  INFO 38860 --- [consultas-service] [ntainer#3-0-C-1] c.r.c.consultas.ProyectorFichas          : Proyección: ficha de luis.morado@uptc.edu.co actualizada (registro)
2026-11-18T10:11:54.749-05:00  INFO 38860 --- [consultas-service] [ntainer#2-0-C-1] c.r.c.consultas.ProyectorFichas          : Proyección: ficha de luis.morado@uptc.edu.co ahora tiene 1 publicaciones
2026-11-18T10:11:59.840-05:00  INFO 38860 --- [consultas-service] [ntainer#2-0-C-1] c.r.c.consultas.ProyectorFichas          : Proyección: ficha de luis.morado@uptc.edu.co ahora tiene 2 publicaciones


Esto cambiaría en cada punto si el modelo de lectura y el de escritura fueran el mismo:

1. **Ficha de un investigador con dos publicaciones:** En lugar de obtener un único documento precalculado con los datos del investigador y sus publicaciones embebidas desde una sola base, sería necesario hacer dos consultas separadas (una a PostgreSQL en `investigadores-service` y otra a MongoDB en `publicaciones-service`) y unir los resultados en memoria en tiempo de ejecución.


2. **El ranking:** En vez de ordenar directamente por el campo desnormalizado `totalPublicaciones` guardado en cada documento de MongoDB, habría que agrupar y contar dinámicamente todas las publicaciones por correo en la colección de escritura y cruzarlas luego con los nombres de los investigadores.


3. **Log de `consultas-service` con las líneas `Proyección: ...`:** No existirían consumidores de eventos ni registros de proyección asíncrona traducidos a una vista materializada, ya que las consultas leerían directamente las tablas y colecciones originales modificadas por los comandos.


4. **Consulta de una ficha eliminada devolviendo 404 después de la Saga:** El 404 reflejaría de manera inmediata y síncrona la ausencia del registro en la base transaccional de `investigadores-service`, sin que existiera una vista intermedia en estado `EN_ELIMINACION` ni consistencia eventual pendiente de propagarse por eventos.


5. **Lista de fichas reconstruida tras el paso 8:** No sería posible eliminar por completo la base de datos de consulta y reconstruirla reproduciendo el historial de eventos de Kafka sin afectar los datos reales, porque borrar la base de lectura significaría destruir la única fuente de verdad del sistema.



### Evidencia del taller 9

Log para Introducción con formato disitnto de json

2026-11-18T14:52:40.066-05:00  INFO 48724 --- [consultas-service] [ntainer#3-0-C-1] o.a.k.c.c.internals.SubscriptionState    : [Consumer clientId=consumer-consultas-service-v2-2, groupId=consultas-service-v2] Resetting offset for partition investigadores.eliminados-0 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[localhost:9092 (id: 1 rack: null isFenced: false)], epoch=0}}.
2026-11-18T14:52:40.067-05:00  INFO 48724 --- [consultas-service] [ntainer#3-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : consultas-service-v2: partitions assigned: [investigadores.eliminados-0]
2026-11-18T14:52:40.519-05:00  INFO 48724 --- [consultas-service] [ntainer#0-0-C-1] c.r.c.consultas.EstadisticasListener     : Estadísticas: alta contada (investigadores.registrados-0-0). Total: 1
2026-11-18T14:52:40.543-05:00  INFO 48724 --- [consultas-service] [ntainer#0-0-C-1] c.r.c.consultas.EstadisticasListener     : Estadísticas: alta contada (investigadores.registrados-0-1). Total: 2
2026-11-18T14:52:40.556-05:00  INFO 48724 --- [consultas-service] [ntainer#0-0-C-1] c.r.c.consultas.EstadisticasListener     : Estadísticas: alta contada (investigadores.registrados-0-2). Total: 3
2026-11-18T14:52:40.570-05:00  INFO 48724 --- [consultas-service] [ntainer#0-0-C-1] c.r.c.consultas.EstadisticasListener     : Estadísticas: alta contada (investigadores.registrados-0-3). Total: 4
2026-11-18T14:52:40.574-05:00  WARN 48724 --- [consultas-service] [ntainer#0-0-C-1] c.r.c.consultas.EstadisticasListener     : Estadísticas: mensaje no válido en investigadores.registrados-0@4, no se cuenta
2026-11-18T14:52:40.589-05:00  INFO 48724 --- [consultas-service] [ntainer#0-0-C-1] c.r.c.consultas.EstadisticasListener     : Estadísticas: alta contada (investigadores.registrados-0-5). Total: 5
2026-11-18T14:52:40.589-05:00  WARN 48724 --- [consultas-service] [ntainer#0-0-C-1] c.r.c.consultas.EstadisticasListener     : Estadísticas: mensaje no válido en investigadores.registrados-0@6, no se cuenta
2026-11-18T14:56:30.075-05:00  WARN 48724 --- [consultas-service] [ntainer#0-0-C-1] c.r.c.consultas.EstadisticasListener     : Estadísticas: mensaje no válido en investigadores.registrados-0@7, no se cuenta
2026-11-18T14:56:30.614-05:00  INFO 48724 --- [consultas-service] [ntainer#2-0-C-1] o.a.kafka.common.config.AbstractConfig   : ProducerConfig values: 

endpoint de estadisticas

{
  "investigadoresRegistrados": 5
}

Taller SAGA - Lección 10

Líneas mostradas al eliminar a Sofia Rios
Publicaciones service: 
2026-11-18T15:02:55.133-05:00  INFO 14688 --- [publicaciones-service] [ntainer#1-0-C-1] c.r.p.p.InvestigadorRegistradoListener   : Evento recibido [investigadores.registrados]: {"id":6,"nombreCompleto":"Sofia Rios","correoInstitucional":"sofia.rios@uptc.edu.co"}
2026-11-18T15:04:52.237-05:00  INFO 14688 --- [publicaciones-service] [ntainer#0-0-C-1] .r.p.p.InvestigadorEnEliminacionListener : Saga: 2 publicaciones de sofia.rios@uptc.edu.co marcadas como huérfanas
2026-11-18T15:05:21.749-05:00  INFO 14688 --- [publicaciones-service] [rap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver      : Resolving eureka endpoints via configuration
investigadores service:

26-11-18T15:02:55.131-05:00  INFO 49092 --- [investigadores-service] [   scheduling-1] c.r.i.i.i.s.mensajeria.OutboxPoller      : Evento 7 publicado en investigadores.registrados
2026-11-18T15:04:44.392-05:00  INFO 49092 --- [investigadores-service] [rap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver      : Resolving eureka endpoints via configuration
2026-11-18T15:04:52.045-05:00  INFO 49092 --- [investigadores-service] [   scheduling-1] c.r.i.i.i.s.mensajeria.OutboxPoller      : Evento 8 publicado en investigadores.en-eliminacion
2026-11-18T15:04:52.342-05:00  INFO 49092 --- [investigadores-service] [ntainer#0-0-C-1] .i.i.i.e.m.PublicacionesMarcadasListener : Saga: publicaciones confirmó para sofia.rios@uptc.edu.co. Eliminando investigador.
2026-11-18T15:04:54.094-05:00  INFO 49092 --- [investigadores-service] [   scheduling-1] c.r.i.i.i.s.mensajeria.OutboxPoller      : Evento 9 publicado en investigadores.eliminados

consultas-service:

2026-11-18T15:02:37.835-05:00  INFO 48724 --- [consultas-service] [rap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver      : Resolving eureka endpoints via configuration
2026-11-18T15:02:55.148-05:00  INFO 48724 --- [consultas-service] [ntainer#0-0-C-1] c.r.c.consultas.EstadisticasListener     : Estadísticas: alta contada (investigadores.registrados-0-8). Total: 6
2026-11-18T15:02:55.182-05:00  INFO 48724 --- [consultas-service] [ntainer#2-0-C-1] c.r.c.consultas.ProyectorFichas          : Proyección: ficha de sofia.rios@uptc.edu.co actualizada (registro)
2026-11-18T15:03:34.467-05:00  INFO 48724 --- [consultas-service] [ntainer#1-0-C-1] c.r.c.consultas.ProyectorFichas          : Proyección: ficha de sofia.rios@uptc.edu.co ahora tiene 1 publicaciones
2026-11-18T15:03:38.701-05:00  INFO 48724 --- [consultas-service] [ntainer#1-0-C-1] c.r.c.consultas.ProyectorFichas          : Proyección: ficha de sofia.rios@uptc.edu.co ahora tiene 2 publicaciones
2026-11-18T15:04:52.062-05:00  INFO 48724 --- [consultas-service] [ntainer#4-0-C-1] c.r.c.consultas.ProyectorFichas          : Proyección: sofia.rios@uptc.edu.co marcado EN_ELIMINACION
2026-11-18T15:04:54.116-05:00  INFO 48724 --- [consultas-service] [ntainer#3-0-C-1] c.r.c.consultas.ProyectorFichas          : Proyección: ficha de sofia.rios@uptc.edu.co eliminada


Taller Lección 11

Responde por escrito, con tus palabras: (a) ¿por qué se copia primero pom.xml y luego src? (b) ¿qué contiene la imagen final y qué no? (c) ¿por qué USER rica?

Docker construye y guarda cada instrucción como una capa en caché. Como el código fuente cambia con mucha frecuencia pero las dependencias del pom.xml casi no varían, copiar y resolver primero el pom.xml permite que Docker reutilice la capa de dependencias descargadas y solo vuelva a compilar nuestro código en construcciones posteriores, ahorrando bastante tiempo.
Gracias a la construcción multietapa (multi-stage build), la imagen final contiene únicamente el entorno de ejecución de Java (eclipse-temurin:25-jre), el archivo compilado app.jar y el usuario de sistema rica. No contiene Maven, ni el JDK completo, ni el código fuente (src), ni los archivos temporales de compilación (target/), lo que hace que la imagen sea mucho más liviana y tenga menos superficie de ataque.
¿Por qué USER rica?
Por seguridad (principio de mínimo privilegio): por defecto, los contenedores ejecutan sus procesos como root, lo que implica que si un atacante logra comprometer la aplicación dentro del contenedor tendría permisos de administrador sobre él. Crear y usar un usuario sin privilegios (rica) limita el daño potencial en caso de una vulnerabilidad.