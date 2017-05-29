CREATE TABLE ninegag (
  id         UUID PRIMARY KEY,
  name       VARCHAR   NOT NULL,
  gifurl     VARCHAR   NOT NULL,
  category_id UUID,
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

CREATE TABLE category (
  id         UUID PRIMARY KEY,
  name           VARCHAR   NOT NULL,
  created_at TIMESTAMP    NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE UNIQUE INDEX rss_name
  ON rss (name);
CREATE UNIQUE INDEX bot_name
  ON bot (name);
CREATE UNIQUE INDEX category_name
  ON category (name);

ALTER TABLE bot_resources
  ADD CONSTRAINT bot_resources_fk FOREIGN KEY (bot_id) REFERENCES bot (id) ON UPDATE RESTRICT ON DELETE CASCADE;
ALTER TABLE ninegag
  ADD CONSTRAINT ninegag_category_fk FOREIGN KEY (category_id) REFERENCES category (id) ON UPDATE RESTRICT ON DELETE CASCADE;

CREATE INDEX ninegag_index_id
  ON ninegag (id);
CREATE INDEX codinglove_index_id
  ON codinglove (id);

-- Create sample data
INSERT INTO category VALUES ('3a053b16-1249-4bfd-9734-bccf25298180', 'funny', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('4d96e54d-c220-497a-a04d-64417d173f7f', 'relationship', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('44b13f55-3eb8-43e0-8d48-483d8ecb5b5f', 'science', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('10cd8a09-1554-4062-8fff-e949a137a2d9', 'funlegacy', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('d5fc320f-1066-46da-ac92-4f81629365be', 'savage', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('708e3775-e5a1-49a0-864f-f7b78257a291', 'superhero', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('c875cf5a-9273-4872-8fb0-c11877d99483', 'girly', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('14e586ad-9a49-4fe4-8044-891fb7605cdd', 'horror', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('ffdf89c7-a39d-4160-a8dd-c576d7821d36', 'imadedis', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('d501ab72-07dc-4930-9c32-3908a5fc9924', 'politics', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('95ca6862-eb48-43ba-a69a-8a3c193d12ff', 'school', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('fa959ee2-dd8d-4134-97b4-31a70729efdc', 'timely', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('c7bcce4e-605c-487d-b3da-fa388b253a93', 'wtf', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('ac9a6a32-52ca-487e-80bd-8566f1a6a786', 'gif', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('c728a29f-aee2-4f5d-a099-d198ffce5acb', 'nsfw', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('33e88518-e2ae-4b7d-a6bd-014bb835e226', 'gaming', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('baed1e8c-d5a2-4012-9d3f-7c4964298dca', 'anime-manga', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('ff92bcd0-0a55-4b50-a5bd-344417a8fc4a', 'movie-tv', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('c780acd2-a4ce-45fe-94f5-b4c8f5bbb40a', 'cute', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('f86f921d-64ca-43be-92df-ab87ef939bcd', 'girl', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('5b38a632-1bb9-4200-b69b-e9ac6eeb0d41', 'awesome', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('c721d3b1-703c-4a3f-beb3-e6f908bde67b', 'sport', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('79f262da-5c14-4aec-9ec4-e795197221e4', 'food', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('4cf01e60-250d-4d05-93ad-1673c40f1243', 'ask9gag', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('4388487c-7dc5-4125-a900-c91bacc0b821', 'darkhumor', '2016-12-19 10:08:07.896', NULL, NULL);
INSERT INTO category VALUES ('cc63616d-6ed7-4b4f-8744-831f71af4b4d', 'country', '2016-12-19 10:08:07.896', NULL, NULL);