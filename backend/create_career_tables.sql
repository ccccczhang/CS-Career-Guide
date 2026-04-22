CREATE TABLE IF NOT EXISTS career_evaluation_careercategory (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  market_demand int(11) NOT NULL DEFAULT 0,
  description longtext NOT NULL,
  `order` int(11) NOT NULL DEFAULT 0,
  created_at datetime(6) NOT NULL,
  updated_at datetime(6) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS career_evaluation_careersubcategory (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  market_demand int(11) NOT NULL DEFAULT 0,
  skills longtext NOT NULL,
  description longtext NOT NULL,
  `order` int(11) NOT NULL DEFAULT 0,
  created_at datetime(6) NOT NULL,
  updated_at datetime(6) NOT NULL,
  category_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY category_id (category_id),
  CONSTRAINT career_evaluation_careersubcategory_category_id_fk FOREIGN KEY (category_id) REFERENCES career_evaluation_careercategory (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
