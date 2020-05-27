DROP TABLE IF EXISTS  `redis_user`;
CREATE TABLE `redis_user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_id` int(10) unsigned  NOT NULL COMMENT '学号',
  `user_name` varchar(25) DEFAULT NULL COMMENT '用户名',
  `description` varchar(25) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT redis_user VALUES (1 ,12305,'蔡小柴','就读于国立中央大学资讯工程');