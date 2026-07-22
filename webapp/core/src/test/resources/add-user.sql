-- Add a user to users table to represent the current user

SET SCHEMA find;

MERGE INTO users VALUES(1, 'user');
-- MERGE with an explicit ID does not advance H2's AUTO_INCREMENT sequence, so
-- the next generated ID would also be 1, causing a primary key violation in
-- tests which add another user.  Resetting the sequence here ensures new rows get IDs > 1.
ALTER TABLE users ALTER COLUMN user_id RESTART WITH 2;
