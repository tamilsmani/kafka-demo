CREATE TABLE TASK (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)  ENGINE=INNODB;

COMMIT


select * from task

delete from task
insert into task(title) values ('Test')

alter table task drop column created_at