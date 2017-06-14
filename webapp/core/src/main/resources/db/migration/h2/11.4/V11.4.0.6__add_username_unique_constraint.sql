/*-------------------------------------------
-- Description: Add UNIQUE and NOT NULL
  constraints to the user table.

-- Previous Version: 11.4.0.5
-- Target Version: 11.4.0.6
--------------------------------------------*/

SET SCHEMA find;

ALTER TABLE users ADD CONSTRAINT unique__users__username UNIQUE (username);
ALTER TABLE users ALTER username SET NOT NULL;