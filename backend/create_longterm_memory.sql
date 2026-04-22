CREATE TABLE IF NOT EXISTS ai_integration_longtermmemory (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  user_id bigint(20) DEFAULT NULL,
  `key` varchar(100) NOT NULL,
  value longtext NOT NULL,
  category varchar(50) NOT NULL,
  conversation_id bigint(20) DEFAULT NULL,
  created_at datetime(6) NOT NULL,
  updated_at datetime(6) NOT NULL,
  PRIMARY KEY (id),
  KEY user_id (user_id),
  KEY conversation_id (conversation_id),
  CONSTRAINT ai_integration_longtermmemory_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT ai_integration_longtermmemory_conversation_id_fk FOREIGN KEY (conversation_id) REFERENCES ai_integration_aiconversation (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;