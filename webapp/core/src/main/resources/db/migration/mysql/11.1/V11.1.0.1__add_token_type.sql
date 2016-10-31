/*-------------------------------------------
-- Description: Add token type column to distinguish
  between promotions and regular state tokens

-- Previous Version: 1
-- Target Version: 11.1.0.1
--------------------------------------------*/

USE find;

ALTER TABLE search_stored_state ADD type VARCHAR(255) NOT NULL DEFAULT 'QUERY';
