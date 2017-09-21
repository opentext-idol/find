/*-------------------------------------------
-- Description: Add username column to
    Users table.

-- Previous Version: 11.4.0.0
-- Target Version: 11.4.0.1
--------------------------------------------*/

USE find;

ALTER TABLE users ADD username TEXT;