/*-------------------------------------------
-- Description: Add document whitelist/blacklist to searches.

-- Previous Version: 11.5.1.0
-- Target Version: 12.6.0.0
--------------------------------------------*/

SET SCHEMA find;

ALTER TABLE searches ADD document_selection_is_whitelist BIT NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS search_document_selection
(
  search_document_selection_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  search_id BIGINT NOT NULL,
  reference NVARCHAR(1000) NOT NULL
);

CREATE INDEX ix__search_document_selection__search_id ON search_document_selection (search_id);

ALTER TABLE search_document_selection
ADD CONSTRAINT fk__search_document_selection__searches
FOREIGN KEY (search_id) REFERENCES searches (search_id)
ON DELETE CASCADE;
