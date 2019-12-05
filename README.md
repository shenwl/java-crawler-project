# Java多线程爬虫
学一个语言时，总得写个爬虫

## Docker使用MySQL示例
### 1. 启动
```bash
docker run --name mysql -v `pwd`/mysql-data:/var/lib/mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -d mysql
```
### 2. 连接数据库，创建news数据库
```mysql
CREATE DATABASE news
```
### 3. migrate
```bash
# mvn flyway:clean
mvn flyway:migrate
```


### tips
- 数据内容有问题可以设置MySQL CHARSET为utf8mb4