/*-------------------------------------------
-- Description: Add geography filters to the saved searches.

-- Previous Version: 11.5.0.0
-- Target Version: 11.5.0.1
--------------------------------------------*/

SET SCHEMA find;

CREATE TABLE IF NOT EXISTS search_geography_filters
(
  search_geography_filter_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  search_id BIGINT NOT NULL,
  field NVARCHAR(21844) NOT NULL,
  json NVARCHAR(21844) NOT NULL
);

CREATE INDEX IF NOT EXISTS ix__search_geography_filters__search_id ON search_geography_filters (search_id);
ALTER TABLE search_geography_filters ADD CONSTRAINT IF NOT EXISTS fk__search_geography_filters__searches FOREIGN KEY (search_id) REFERENCES searches (search_id) ON DELETE CASCADE;
