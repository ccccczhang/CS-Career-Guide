CREATE TABLE IF NOT EXISTS ai_integration_aiconversation (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  session_id varchar(100) NOT NULL,
  mode varchar(50) NOT NULL,
  created_at datetime(6) NOT NULL,
  updated_at datetime(6) NOT NULL,
  user_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY session_id (session_id),
  KEY user_id (user_id),
  CONSTRAINT ai_integration_aiconversation_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_integration_aichatlog (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  message_type varchar(20) NOT NULL,
  content longtext NOT NULL,
  timestamp datetime(6) NOT NULL,
  conversation_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY conversation_id (conversation_id),
  CONSTRAINT ai_integration_aichatlog_conversation_id_fk FOREIGN KEY (conversation_id) REFERENCES ai_integration_aiconversation (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
