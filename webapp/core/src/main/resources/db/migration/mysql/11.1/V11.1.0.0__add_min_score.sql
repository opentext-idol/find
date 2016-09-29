/*-------------------------------------------
-- Description: Add min score column to
  searches to accommodate extra restriction.

-- Previous Version: 11.0.0.0
-- Target Version: 11.1.0.0
--------------------------------------------*/

USE find;

ALTER TABLE searches ADD min_score INT NOT NULL DEFAULT 0;
