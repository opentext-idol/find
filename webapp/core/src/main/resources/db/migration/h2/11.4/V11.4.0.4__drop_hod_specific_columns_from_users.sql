/*-------------------------------------------
-- Description: Remove the hod specific and
  unnecessary IDOL fields.

-- Previous Version: 11.4.0.3
-- Target Version: 11.4.0.4
--------------------------------------------*/

SET SCHEMA find;

ALTER TABLE users DROP COLUMN domain;
ALTER TABLE users DROP COLUMN user_store;
ALTER TABLE users DROP COLUMN uuid;
ALTER TABLE users DROP COLUMN uid;
