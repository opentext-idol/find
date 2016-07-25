/*-------------------------------------------
-- Description: Add parametric ranges table
  for handling numeric and date range restrictions

-- Previous Version: 11.1.0.3
-- Target Version: 11.1.0.4
--------------------------------------------*/

SET SCHEMA find;

ALTER TABLE search_parametric_ranges ALTER COLUMN min DOUBLE NOT NULL;
ALTER TABLE search_parametric_ranges ALTER COLUMN max DOUBLE NOT NULL;