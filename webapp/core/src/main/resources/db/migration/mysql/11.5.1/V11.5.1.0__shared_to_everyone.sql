/*-------------------------------------------
-- Description: Add a shared-to-everyone union
  table which contains information about a
  saved search's global sharing permissions.

-- Previous Version: 11.5.0.1
-- Target Version: 11.5.1.0
--------------------------------------------*/

USE find;

CREATE TABLE shared_to_everyone
(
  search_id     BIGINT NOT NULL,
  shared_date   DATETIME,
  modified_date DATETIME
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE INDEX ix__shared_to_everyone__search_id ON shared_to_everyone (search_id);
ALTER TABLE shared_to_everyone ADD CONSTRAINT fk__shared_to_everyone__searches FOREIGN KEY (search_id) REFERENCES searches (search_id) ON DELETE CASCADE;
ALTER TABLE shared_to_everyone ADD PRIMARY KEY (search_id);
