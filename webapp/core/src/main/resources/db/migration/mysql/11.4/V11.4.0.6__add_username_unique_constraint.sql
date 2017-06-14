/*-------------------------------------------
-- Description: Add UNIQUE and NOT NULL
  constraints to the user table.

-- Previous Version: 11.4.0.5
-- Target Version: 11.4.0.6
--------------------------------------------*/

USE find;

ALTER TABLE users MODIFY COLUMN username VARCHAR(120) NOT NULL;
ALTER TABLE users ADD CONSTRAINT unique__users__username UNIQUE (username);