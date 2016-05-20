/*-------------------------------------------
-- Description: Add min score column to
  searches to accommodate extra restriction.

-- Previous Version: 0
-- Target Version: 1
--------------------------------------------*/

USE find;

ALTER TABLE searches ADD min_score INT NOT NULL DEFAULT 0;
