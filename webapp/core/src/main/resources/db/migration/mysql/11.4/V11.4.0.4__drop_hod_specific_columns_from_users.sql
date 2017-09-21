/*-------------------------------------------
-- Description: Remove the hod specific and
  unnecessary IDOL fields.

-- Previous Version: 11.4.0.3
-- Target Version: 11.4.0.4
--------------------------------------------*/

USE find;

ALTER TABLE users DROP COLUMN domain, DROP COLUMN user_store, DROP COLUMN uuid, DROP COLUMN uid;
