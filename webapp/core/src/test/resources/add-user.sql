-- Add a user to users table to represent the current user

SET SCHEMA find;

MERGE INTO users VALUES(1, 'user');
