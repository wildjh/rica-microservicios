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