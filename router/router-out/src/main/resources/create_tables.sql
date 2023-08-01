create table router_endpoint_config
(
    endpoint_id              varchar(32)       not null comment '监听端点ID，主键'
        primary key,
    max_concurrent_consumers int default 2     not null comment '最大消费者数量，默认2',
    prefetch_count           int default 100   not null comment '预读取消息数量，默认100',
    batch_size               int default 50    not null comment '批量接收方法的消息数量，默认50',
    receive_timeout          int default 15000 not null comment '监听等待时长，单位：毫秒，默认15秒',
    queue_names              varchar(1024)     null comment '监听的队列，多个使用,分隔',
    listen                   int default 0     not null comment '是否监听，0-不监听，1-监听',
    endpoint_description     varchar(512)      not null comment '监听端点描述'
)
    comment '路由监听队列配置表';

