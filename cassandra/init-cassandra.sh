#!/usr/bin/env bash

set -euo pipefail

readonly CASSANDRA_HOST="${CASSANDRA_HOSTS}"
readonly CQLSH_PORT="${CASSANDRA_PORT}"

cqlsh_args=("$CASSANDRA_HOST" "$CQLSH_PORT" "-u" "$CASSANDRA_USERNAME" "-p" "${CASSANDRA_PASSWORD}")

wait_for_cassandra() {
  local attempt

  for attempt in $(seq 1 60); do
    if cqlsh "${cqlsh_args[@]}" -e "DESCRIBE KEYSPACES" >/dev/null 2>&1; then
      return 0
    fi
    echo "Waiting for Cassandra (${attempt}/60)..."
    sleep 5
  done

  echo "Cassandra is not ready after waiting" >&2
  return 1
}

apply_migrations() {
  cqlsh "${cqlsh_args[@]}" <<CQL
CREATE KEYSPACE IF NOT EXISTS ${CASSANDRA_KEYSPACE}
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE ${CASSANDRA_KEYSPACE};

CREATE TABLE IF NOT EXISTS event_reactions (
  event_id text,
  created_by text,
  like_value tinyint,
  created_at timestamp,
  PRIMARY KEY ((event_id), created_by)
);

CREATE INDEX IF NOT EXISTS event_reactions_like_value_idx
ON event_reactions (like_value);

CREATE INDEX IF NOT EXISTS event_reactions_created_by_idx
ON event_reactions (created_by);
CQL
}

wait_for_cassandra
apply_migrations
