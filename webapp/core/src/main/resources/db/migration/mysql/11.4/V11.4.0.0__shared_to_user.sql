/*-------------------------------------------
-- Description: Add a shared to user union
  table which contains information about a
  user - saved search pair and the type of
  permission the user has (can edit/can't edit).

-- Previous Version: 11.2.0.0
-- Target Version: 11.4.0.0
--------------------------------------------*/

USE find;

CREATE TABLE shared_to_users
(
  search_id     BIGINT NOT NULL,
  user_id       BIGINT NOT NULL,
  shared_date   DATETIME,
  modified_date DATETIME,
  can_edit      BIT    NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE INDEX ix__shared_to_users__search_id ON shared_to_users (search_id);
CREATE INDEX ix__shared_to_users__user_id ON shared_to_users (user_id);
ALTER TABLE shared_to_users ADD CONSTRAINT fk__shared_to_users__searches FOREIGN KEY (search_id) REFERENCES searches (search_id);
ALTER TABLE shared_to_users ADD CONSTRAINT fk__shared_to_users__users FOREIGN KEY (user_id) REFERENCES users (user_id);
ALTER TABLE shared_to_users ADD PRIMARY KEY (search_id, user_id);