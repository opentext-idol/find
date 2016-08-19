/*-------------------------------------------
-- Description: Add parametric ranges table
  for handling numeric and date range restrictions

-- Previous Version: 11.1.0.3
-- Target Version: 11.1.0.4
--------------------------------------------*/

USE find;

ALTER TABLE search_parametric_ranges MODIFY COLUMN min DOUBLE NOT NULL;
ALTER TABLE search_parametric_ranges MODIFY COLUMN max DOUBLE NOT NULL;