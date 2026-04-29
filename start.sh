#!/bin/bash

if [ -n "$DATABASE_URL" ]; then
  PROTO="$(echo $DATABASE_URL | grep :// | sed -e 's,^\(.*://\).*,\1,g')"
  URL_NO_PROTO="$(echo ${DATABASE_URL/$PROTO/})"
  USERPASS="$(echo $URL_NO_PROTO | grep @ | cut -d@ -f1)"
  DB_USER="$(echo $USERPASS | cut -d: -f1)"
  DB_PASS="$(echo $USERPASS | cut -d: -f2)"
  HOSTPORT="$(echo ${URL_NO_PROTO/$USERPASS@/} | cut -d/ -f1)"
  DB_HOST="$(echo $HOSTPORT | cut -d: -f1)"
  DB_PORT="$(echo $HOSTPORT | cut -d: -f2)"
  DB_NAME="$(echo $URL_NO_PROTO | grep / | cut -d/ -f2)"

  export PGHOST="$DB_HOST"
  export PGPORT="$DB_PORT"
  export PGUSER="$DB_USER"
  export PGPASSWORD="$DB_PASS"
  export PGDATABASE="$DB_NAME"
fi

exec java -Djava.security.egd=file:/dev/./urandom \
  -jar /app/backend.jar \
  --server.port=${PORT:-8080} \
  --spring.datasource.url=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE} \
  --spring.datasource.username=${PGUSER} \
  --spring.datasource.password=${PGPASSWORD}
