/*-------------------------------------------
-- Description: Add geography filters to the saved searches.

-- Previous Version: 11.5.0.0
-- Target Version: 11.5.0.1
--------------------------------------------*/

USE find;

CREATE TABLE search_geography_filters
(
  search_geography_filter_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  search_id BIGINT NOT NULL,
  field TEXT NOT NULL,
  json TEXT NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE INDEX ix__search_geography_filters__search_id ON search_geography_filters (search_id);
ALTER TABLE search_geography_filters ADD CONSTRAINT fk__search_geography_filters__searches FOREIGN KEY (search_id) REFERENCES searches (search_id) ON DELETE CASCADE;
