INSERT INTO router_endpoint_config (endpoint_id, max_concurrent_consumers, prefetch_count, batch_size, receive_timeout, queue_names, listen, endpoint_description) VALUES ('demo', 4, 200, 50, 15000, 'demo_queue', 1, '测试监听');