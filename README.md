# Ktor Watcher

This is a utility for developers who have hosted services and would like to have peace of mind over their continuity. Current support is limited to monitoring of HTTP endpoints and persistence is limited to MySQL.

![image](https://user-images.githubusercontent.com/32527177/212494693-ed704c77-aa34-42a6-9098-a4b5923fe5ec.png)


# Configuration

Application configuration can be found [here](https://github.com/gosre/ktor-watcher/blob/main/src/main/resources/watcher.yaml)
```
displayPeriod: 60  # in days
port: 8080  
mysql:  
	host: "localhost"  
	port: 3306
	username: "database_user"  
	password: "database_password"  
	database: "database_name"
```

### MySQL Tables

The following statements can be used to populate your database with the current tables needed to start using ktor-watcher.
```
CREATE TABLE `watcher` (  
  `id` int NOT NULL AUTO_INCREMENT,  
  `name` varchar(50) NOT NULL DEFAULT 'Endpoint Name',  
  `endpoint` varchar(100) NOT NULL,  
  `update_interval` int NOT NULL DEFAULT '5',  
  `status` enum('OPERATIONAL','OFFLINE') NOT NULL DEFAULT 'OFFLINE',  
  PRIMARY KEY (`id`),  
  UNIQUE KEY `identifier_UNIQUE` (`id`)  
)
```
```
CREATE TABLE `watcher_day` (  
  `watcher_id` int NOT NULL,  
  `date` date NOT NULL,  
  `downtime` int NOT NULL DEFAULT '0',  
  PRIMARY KEY (`watcher_id`,`date`),  
  CONSTRAINT `fkey_id` FOREIGN KEY (`watcher_id`) REFERENCES `watcher` (`id`)  
)
```

### Creating a Watcher

Insert a new row into the ``watcher`` table with a name, endpoint url, and update interval (minutes) (optional). Restart the application.
