CREATE TABLE ninegag (
  id         UUID PRIMARY KEY,
  name       VARCHAR   NOT NULL,
  gifurl     VARCHAR   NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
  deleted_at TIMESTAMP
);

CREATE TABLE codinglove (
  id         UUID PRIMARY KEY,
  name       VARCHAR   NOT NULL,
  gifurl     VARCHAR   NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
  deleted_at TIMESTAMP
);

CREATE TABLE rss (
  id             UUID PRIMARY KEY,
  name           VARCHAR   NOT NULL,
  rss_url        VARCHAR   NOT NULL,
  incoming_token VARCHAR   NOT NULL,
  created_at     TIMESTAMP NOT NULL DEFAULT current_timestamp,
  updated_at     TIMESTAMP,
  deleted_at     TIMESTAMP
);

CREATE TABLE bot (
  id         UUID PRIMARY KEY,
  name           VARCHAR   NOT NULL,
  created_at TIMESTAMP    NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE TABLE bot_resources (
  id         UUID PRIMARY KEY,
  bot_id UUID,
  value           VARCHAR   NOT NULL,
  created_at TIMESTAMP    NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);


CREATE UNIQUE INDEX rss_name
  ON rss (name);
CREATE UNIQUE INDEX bot_name
  ON bot (name);

ALTER TABLE bot_resources
  ADD CONSTRAINT bot_resources_fk FOREIGN KEY (bot_id) REFERENCES bot (id) ON UPDATE RESTRICT ON DELETE CASCADE;

CREATE INDEX ninegag_index_id
  ON ninegag (id);
CREATE INDEX codinglove_index_id
  ON codinglove (id);