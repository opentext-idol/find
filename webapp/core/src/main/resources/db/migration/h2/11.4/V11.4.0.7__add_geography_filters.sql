/*-------------------------------------------
-- Description: Add geography filters to the saved searches.

-- Previous Version: 11.4.0.6
-- Target Version: 11.4.0.7
--------------------------------------------*/

SET SCHEMA find;

CREATE TABLE search_geography_filters
(
  search_geography_filter_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  search_id BIGINT NOT NULL,
  field NVARCHAR(21844) NOT NULL,
  json NVARCHAR(21844) NOT NULL
);

CREATE INDEX ix__search_geography_filters__search_id ON search_geography_filters (search_id);
ALTER TABLE search_geography_filters ADD CONSTRAINT fk__search_geography_filters__searches FOREIGN KEY (search_id) REFERENCES searches (search_id) ON DELETE CASCADE;
