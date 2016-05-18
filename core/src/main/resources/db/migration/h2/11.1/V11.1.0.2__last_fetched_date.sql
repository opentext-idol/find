/*-------------------------------------------
-- Description: Add last fetched date for
  tracking when a query was last run

-- Previous Version: 1
-- Target Version: 11.1.0.2
--------------------------------------------*/

SET SCHEMA find;

ALTER TABLE searches ADD last_fetched_date DATETIME;

UPDATE searches SET last_fetched_date = CURRENT_TIMESTAMP();
