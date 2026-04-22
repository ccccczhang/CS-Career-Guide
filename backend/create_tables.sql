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

CREATE TABLE IF NOT EXISTS career_paths_careerpath (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  path_type varchar(20) NOT NULL,
  title varchar(100) NOT NULL,
  description longtext NOT NULL,
  preparation_advice longtext NOT NULL,
  time_plan json NOT NULL,
  resource_links json NOT NULL,
  created_at datetime(6) NOT NULL,
  updated_at datetime(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY path_type (path_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS career_paths_careerassessment (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  question longtext NOT NULL,
  options json NOT NULL,
  path_weights json NOT NULL,
  `order` int(11) NOT NULL DEFAULT 0,
  is_active tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS career_paths_assessmentresult (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  session_id varchar(100) NOT NULL,
  answers json NOT NULL,
  recommended_path varchar(20) NOT NULL,
  confidence_score double NOT NULL,
  created_at datetime(6) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;